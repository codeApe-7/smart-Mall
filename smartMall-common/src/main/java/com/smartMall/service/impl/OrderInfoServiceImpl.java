package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.OrderItem;
import com.smartMall.entities.dto.CartDeleteDTO;
import com.smartMall.entities.dto.OrderCancelDTO;
import com.smartMall.entities.dto.OrderCreateDTO;
import com.smartMall.entities.dto.OrderPreviewDTO;
import com.smartMall.entities.dto.OrderQueryDTO;
import com.smartMall.entities.enums.OrderStatusEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.OrderCreateVO;
import com.smartMall.entities.vo.OrderDetailVO;
import com.smartMall.entities.vo.OrderInfoListVO;
import com.smartMall.entities.vo.OrderItemVO;
import com.smartMall.entities.vo.OrderPreviewVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ShoppingCartItemVO;
import com.smartMall.entities.vo.ShoppingCartVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.mapper.OrderInfoMapper;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.OrderItemService;
import com.smartMall.service.ShoppingCartService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * 订单主表 Service 实现。
 */
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {

    private static final int ORDER_NO_SUFFIX_LENGTH = 6;
    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private ShoppingCartService shoppingCartService;

    @Resource
    private OrderItemService orderItemService;

    @Override
    public OrderPreviewVO previewOrder(OrderPreviewDTO dto) {
        List<ShoppingCartItemVO> selectedItems = loadSelectedCartItems(dto.getUserId(), dto.getCartIds(), true);
        OrderPreviewVO orderPreviewVO = new OrderPreviewVO();
        orderPreviewVO.setItems(buildOrderItemVOList(selectedItems));
        orderPreviewVO.setTotalQuantity(selectedItems.stream().mapToInt(ShoppingCartItemVO::getQuantity).sum());
        orderPreviewVO.setTotalAmount(calculateTotalAmount(selectedItems));
        log.info("preview order, userId={}, itemCount={}", dto.getUserId(), selectedItems.size());
        return orderPreviewVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(OrderCreateDTO dto) {
        List<ShoppingCartItemVO> selectedItems = loadSelectedCartItems(dto.getUserId(), dto.getCartIds(), true);
        BigDecimal totalAmount = calculateTotalAmount(selectedItems);
        int totalQuantity = selectedItems.stream().mapToInt(ShoppingCartItemVO::getQuantity).sum();
        Date now = new Date();

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderId(StringTools.getRandomNumber(LENGTH_32));
        orderInfo.setOrderNo(generateOrderNo());
        orderInfo.setUserId(dto.getUserId());
        orderInfo.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getStatus());
        orderInfo.setTotalAmount(totalAmount);
        orderInfo.setTotalQuantity(totalQuantity);
        orderInfo.setReceiverName(dto.getReceiverName());
        orderInfo.setReceiverPhone(dto.getReceiverPhone());
        orderInfo.setReceiverAddress(dto.getReceiverAddress());
        orderInfo.setOrderRemark(dto.getOrderRemark());
        orderInfo.setCreateTime(now);
        orderInfo.setUpdateTime(now);
        this.save(orderInfo);

        List<OrderItem> orderItems = buildOrderItems(orderInfo.getOrderId(), selectedItems, now);
        orderItemService.saveBatch(orderItems);

        CartDeleteDTO cartDeleteDTO = new CartDeleteDTO();
        cartDeleteDTO.setUserId(dto.getUserId());
        cartDeleteDTO.setCartIds(dto.getCartIds());
        shoppingCartService.deleteItems(cartDeleteDTO);

        OrderCreateVO orderCreateVO = new OrderCreateVO();
        orderCreateVO.setOrderId(orderInfo.getOrderId());
        orderCreateVO.setOrderNo(orderInfo.getOrderNo());
        orderCreateVO.setOrderStatus(orderInfo.getOrderStatus());
        orderCreateVO.setTotalAmount(orderInfo.getTotalAmount());
        log.info("create order success, userId={}, orderId={}, totalAmount={}", dto.getUserId(), orderInfo.getOrderId(), totalAmount);
        return orderCreateVO;
    }

    @Override
    public PageResultVO<OrderInfoListVO> loadOrderList(OrderQueryDTO dto) {
        OrderQueryDTO safeQuery = dto == null ? new OrderQueryDTO() : dto;
        validateUserId(safeQuery.getUserId());
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, safeQuery.getUserId())
                .eq(safeQuery.getOrderStatus() != null, OrderInfo::getOrderStatus, safeQuery.getOrderStatus())
                .orderByDesc(OrderInfo::getCreateTime);
        Page<OrderInfo> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        this.page(page, queryWrapper);
        if (page.getRecords().isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }
        List<OrderInfoListVO> records = page.getRecords().stream()
                .map(this::buildOrderInfoListVO)
                .toList();
        return new PageResultVO<>(safeQuery.getPageNo(), safeQuery.getPageSize(), page.getTotal(), records);
    }

    @Override
    public OrderDetailVO getOrderDetail(String userId, String orderId) {
        OrderInfo orderInfo = getOwnedOrder(userId, orderId);
        List<OrderItemVO> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByAsc(OrderItem::getCreateTime))
                .stream()
                .map(this::buildOrderItemVO)
                .toList();
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        BeanUtils.copyProperties(orderInfo, orderDetailVO);
        orderDetailVO.setOrderStatusDesc(getOrderStatusDesc(orderInfo.getOrderStatus()));
        orderDetailVO.setItems(items);
        return orderDetailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(OrderCancelDTO dto) {
        OrderInfo orderInfo = getOwnedOrder(dto.getUserId(), dto.getOrderId());
        if (!OrderStatusEnum.PENDING_PAYMENT.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order can not be canceled");
        }
        Date now = new Date();
        orderInfo.setOrderStatus(OrderStatusEnum.CANCELED.getStatus());
        orderInfo.setCancelTime(now);
        orderInfo.setUpdateTime(now);
        this.updateById(orderInfo);
        log.info("cancel order success, userId={}, orderId={}", dto.getUserId(), dto.getOrderId());
    }

    @Override
    public OrderInfo getUserOrder(String userId, String orderId) {
        return getOwnedOrder(userId, orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOrderPaid(String orderId, Date payTime) {
        if (StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "orderId is required");
        }
        OrderInfo orderInfo = this.getById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "order not found");
        }
        if (OrderStatusEnum.PAID.getStatus().equals(orderInfo.getOrderStatus())) {
            return;
        }
        if (!OrderStatusEnum.PENDING_PAYMENT.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support payment");
        }
        Date finalPayTime = payTime == null ? new Date() : payTime;
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfo.setPayTime(finalPayTime);
        orderInfo.setUpdateTime(finalPayTime);
        this.updateById(orderInfo);
        log.info("mark order paid, orderId={}, payTime={}", orderId, finalPayTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOrderRefundRequested(String orderId, Date refundTime) {
        if (StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "orderId is required");
        }
        OrderInfo orderInfo = this.getById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "order not found");
        }
        if (!OrderStatusEnum.PAID.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support refund request");
        }
        Date now = refundTime == null ? new Date() : refundTime;
        orderInfo.setOrderStatus(OrderStatusEnum.REFUND_REQUESTED.getStatus());
        orderInfo.setRefundTime(now);
        orderInfo.setUpdateTime(now);
        this.updateById(orderInfo);
        log.info("mark order refund requested, orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOrderRefunded(String orderId, Date refundTime) {
        if (StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "orderId is required");
        }
        OrderInfo orderInfo = this.getById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "order not found");
        }
        if (!OrderStatusEnum.REFUND_REQUESTED.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support refund approval");
        }
        Date now = refundTime == null ? new Date() : refundTime;
        orderInfo.setOrderStatus(OrderStatusEnum.REFUNDED.getStatus());
        orderInfo.setRefundTime(now);
        orderInfo.setUpdateTime(now);
        this.updateById(orderInfo);
        log.info("mark order refunded, orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revertOrderFromRefund(String orderId) {
        if (StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "orderId is required");
        }
        OrderInfo orderInfo = this.getById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "order not found");
        }
        if (!OrderStatusEnum.REFUND_REQUESTED.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order is not in refund requested status");
        }
        Date now = new Date();
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfo.setRefundTime(null);
        orderInfo.setUpdateTime(now);
        this.updateById(orderInfo);
        log.info("revert order from refund, orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOrderShipped(String orderId, Date shipTime) {
        if (StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "orderId is required");
        }
        OrderInfo orderInfo = this.getById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "order not found");
        }
        if (!OrderStatusEnum.PAID.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support shipping");
        }
        Date now = shipTime == null ? new Date() : shipTime;
        orderInfo.setOrderStatus(OrderStatusEnum.SHIPPED.getStatus());
        orderInfo.setShipTime(now);
        orderInfo.setUpdateTime(now);
        this.updateById(orderInfo);
        log.info("mark order shipped, orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOrderReceived(String orderId, Date receiveTime) {
        if (StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "orderId is required");
        }
        OrderInfo orderInfo = this.getById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "order not found");
        }
        if (!OrderStatusEnum.SHIPPED.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support receiving");
        }
        Date now = receiveTime == null ? new Date() : receiveTime;
        orderInfo.setOrderStatus(OrderStatusEnum.RECEIVED.getStatus());
        orderInfo.setReceiveTime(now);
        orderInfo.setUpdateTime(now);
        this.updateById(orderInfo);
        log.info("mark order received, orderId={}", orderId);
    }

    private List<ShoppingCartItemVO> loadSelectedCartItems(String userId, List<String> cartIds, boolean requireAvailable) {
        validateUserId(userId);
        if (cartIds == null || cartIds.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "cartIds can not be empty");
        }
        ShoppingCartVO shoppingCartVO = shoppingCartService.loadCart(userId);
        Set<String> cartIdSet = new HashSet<>(cartIds);
        List<ShoppingCartItemVO> selectedItems = shoppingCartVO.getItems().stream()
                .filter(item -> cartIdSet.contains(item.getCartId()))
                .toList();
        if (selectedItems.isEmpty() || selectedItems.size() != cartIdSet.size()) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "selected cart items not found");
        }
        for (ShoppingCartItemVO item : selectedItems) {
            if (!Boolean.TRUE.equals(item.getSelected())) {
                throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "cart item is not selected");
            }
            if (requireAvailable && !Boolean.TRUE.equals(item.getAvailable())) {
                throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "cart item is unavailable");
            }
        }
        return selectedItems;
    }

    private BigDecimal calculateTotalAmount(List<ShoppingCartItemVO> items) {
        return items.stream()
                .map(ShoppingCartItemVO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderItemVO> buildOrderItemVOList(List<ShoppingCartItemVO> cartItems) {
        return cartItems.stream().map(this::buildOrderItemVO).toList();
    }

    private OrderItemVO buildOrderItemVO(ShoppingCartItemVO cartItem) {
        OrderItemVO orderItemVO = new OrderItemVO();
        orderItemVO.setProductId(cartItem.getProductId());
        orderItemVO.setProductName(cartItem.getProductName());
        orderItemVO.setProductCover(cartItem.getProductCover());
        orderItemVO.setPropertyValueIdHash(cartItem.getPropertyValueIdHash());
        orderItemVO.setPropertyValueIds(cartItem.getPropertyValueIds());
        orderItemVO.setSkuPropertyText(cartItem.getSkuPropertyText());
        orderItemVO.setPrice(cartItem.getPrice());
        orderItemVO.setQuantity(cartItem.getQuantity());
        orderItemVO.setTotalAmount(cartItem.getTotalAmount());
        return orderItemVO;
    }

    private OrderItemVO buildOrderItemVO(OrderItem orderItem) {
        OrderItemVO orderItemVO = new OrderItemVO();
        BeanUtils.copyProperties(orderItem, orderItemVO);
        return orderItemVO;
    }

    private List<OrderItem> buildOrderItems(String orderId, List<ShoppingCartItemVO> selectedItems, Date now) {
        List<OrderItem> orderItems = new ArrayList<>(selectedItems.size());
        for (ShoppingCartItemVO item : selectedItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(StringTools.getRandomNumber(LENGTH_32));
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setProductCover(item.getProductCover());
            orderItem.setPropertyValueIdHash(item.getPropertyValueIdHash());
            orderItem.setPropertyValueIds(item.getPropertyValueIds());
            orderItem.setSkuPropertyText(item.getSkuPropertyText());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalAmount(item.getTotalAmount());
            orderItem.setCreateTime(now);
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    private OrderInfoListVO buildOrderInfoListVO(OrderInfo orderInfo) {
        OrderInfoListVO orderInfoListVO = new OrderInfoListVO();
        BeanUtils.copyProperties(orderInfo, orderInfoListVO);
        orderInfoListVO.setOrderStatusDesc(getOrderStatusDesc(orderInfo.getOrderStatus()));
        return orderInfoListVO;
    }

    private OrderInfo getOwnedOrder(String userId, String orderId) {
        validateUserId(userId);
        if (StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "orderId is required");
        }
        OrderInfo orderInfo = this.getOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getOrderId, orderId));
        if (orderInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "order not found");
        }
        return orderInfo;
    }

    private String getOrderStatusDesc(Integer orderStatus) {
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.getByStatus(orderStatus);
        return orderStatusEnum == null ? "unknown" : orderStatusEnum.getDesc();
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_TIME_FORMATTER) + StringTools.getRandomNumber(ORDER_NO_SUFFIX_LENGTH);
    }

    private void validateUserId(String userId) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "userId is required");
        }
    }
}
