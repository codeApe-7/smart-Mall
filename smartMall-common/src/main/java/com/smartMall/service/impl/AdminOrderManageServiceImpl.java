package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.RefundInfo;
import com.smartMall.entities.dto.AdminOrderQueryDTO;
import com.smartMall.entities.dto.AdminRefundQueryDTO;
import com.smartMall.entities.dto.AdminShipOrderDTO;
import com.smartMall.entities.dto.RefundAuditDTO;
import com.smartMall.entities.dto.ShipOrderDTO;
import com.smartMall.entities.enums.OrderStatusEnum;
import com.smartMall.entities.enums.RefundStatusEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.AdminOrderListVO;
import com.smartMall.entities.vo.AdminRefundInfoVO;
import com.smartMall.entities.vo.OrderDetailVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ShippingInfoVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.AdminOrderManageService;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.RefundInfoService;
import com.smartMall.service.ShippingInfoService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Admin order manage service implementation.
 */
@Service
public class AdminOrderManageServiceImpl implements AdminOrderManageService {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private ShippingInfoService shippingInfoService;

    @Resource
    private RefundInfoService refundInfoService;

    @Override
    public PageResultVO<AdminOrderListVO> loadOrderList(AdminOrderQueryDTO dto) {
        AdminOrderQueryDTO safeQuery = dto == null ? new AdminOrderQueryDTO() : dto;
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<OrderInfo>()
                .like(StringTools.isNotEmpty(safeQuery.getOrderNo()), OrderInfo::getOrderNo, safeQuery.getOrderNo())
                .eq(StringTools.isNotEmpty(safeQuery.getUserId()), OrderInfo::getUserId, safeQuery.getUserId())
                .eq(safeQuery.getOrderStatus() != null, OrderInfo::getOrderStatus, safeQuery.getOrderStatus())
                .orderByDesc(OrderInfo::getCreateTime);
        Page<OrderInfo> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        orderInfoService.page(page, queryWrapper);
        if (page.getRecords().isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }
        return new PageResultVO<>(safeQuery.getPageNo(), safeQuery.getPageSize(), page.getTotal(),
                page.getRecords().stream().map(this::buildOrderListVO).toList());
    }

    @Override
    public OrderDetailVO getOrderDetail(String orderId) {
        OrderInfo orderInfo = getOrderById(orderId);
        return orderInfoService.getOrderDetail(orderInfo.getUserId(), orderInfo.getOrderId());
    }

    @Override
    public ShippingInfoVO shipOrder(AdminShipOrderDTO dto) {
        OrderInfo orderInfo = getOrderById(dto.getOrderId());
        ShipOrderDTO shipOrderDTO = new ShipOrderDTO();
        shipOrderDTO.setOrderId(orderInfo.getOrderId());
        shipOrderDTO.setUserId(orderInfo.getUserId());
        shipOrderDTO.setShippingCompany(dto.getShippingCompany());
        return shippingInfoService.shipOrder(shipOrderDTO);
    }

    @Override
    public ShippingInfoVO getShippingDetail(String orderId) {
        OrderInfo orderInfo = getOrderById(orderId);
        return shippingInfoService.getShippingDetail(orderInfo.getUserId(), orderInfo.getOrderId());
    }

    @Override
    public PageResultVO<AdminRefundInfoVO> loadRefundList(AdminRefundQueryDTO dto) {
        AdminRefundQueryDTO safeQuery = dto == null ? new AdminRefundQueryDTO() : dto;
        LambdaQueryWrapper<RefundInfo> queryWrapper = new LambdaQueryWrapper<RefundInfo>()
                .like(StringTools.isNotEmpty(safeQuery.getRefundNo()), RefundInfo::getRefundNo, safeQuery.getRefundNo())
                .like(StringTools.isNotEmpty(safeQuery.getOrderNo()), RefundInfo::getOrderNo, safeQuery.getOrderNo())
                .eq(StringTools.isNotEmpty(safeQuery.getUserId()), RefundInfo::getUserId, safeQuery.getUserId())
                .eq(safeQuery.getRefundStatus() != null, RefundInfo::getRefundStatus, safeQuery.getRefundStatus())
                .orderByDesc(RefundInfo::getCreateTime);
        Page<RefundInfo> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        refundInfoService.page(page, queryWrapper);
        if (page.getRecords().isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }
        return new PageResultVO<>(safeQuery.getPageNo(), safeQuery.getPageSize(), page.getTotal(),
                page.getRecords().stream().map(this::buildRefundInfoVO).toList());
    }

    @Override
    public AdminRefundInfoVO getRefundDetail(String refundId) {
        return buildRefundInfoVO(getRefundById(refundId));
    }

    @Override
    public void approveRefund(String refundId) {
        RefundInfo refundInfo = getRefundById(refundId);
        RefundAuditDTO dto = new RefundAuditDTO();
        dto.setRefundId(refundInfo.getRefundId());
        dto.setUserId(refundInfo.getUserId());
        refundInfoService.approveRefund(dto);
    }

    @Override
    public void rejectRefund(String refundId) {
        RefundInfo refundInfo = getRefundById(refundId);
        RefundAuditDTO dto = new RefundAuditDTO();
        dto.setRefundId(refundInfo.getRefundId());
        dto.setUserId(refundInfo.getUserId());
        refundInfoService.rejectRefund(dto);
    }

    private OrderInfo getOrderById(String orderId) {
        if (StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "orderId is required");
        }
        OrderInfo orderInfo = orderInfoService.getById(orderId);
        if (orderInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "order not found");
        }
        return orderInfo;
    }

    private RefundInfo getRefundById(String refundId) {
        if (StringTools.isEmpty(refundId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "refundId is required");
        }
        RefundInfo refundInfo = refundInfoService.getById(refundId);
        if (refundInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "refund record not found");
        }
        return refundInfo;
    }

    private AdminOrderListVO buildOrderListVO(OrderInfo orderInfo) {
        AdminOrderListVO vo = new AdminOrderListVO();
        BeanUtils.copyProperties(orderInfo, vo);
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.getByStatus(orderInfo.getOrderStatus());
        vo.setOrderStatusDesc(orderStatusEnum == null ? "unknown" : orderStatusEnum.getDesc());
        return vo;
    }

    private AdminRefundInfoVO buildRefundInfoVO(RefundInfo refundInfo) {
        AdminRefundInfoVO vo = new AdminRefundInfoVO();
        BeanUtils.copyProperties(refundInfo, vo);
        RefundStatusEnum refundStatusEnum = RefundStatusEnum.getByStatus(refundInfo.getRefundStatus());
        vo.setRefundStatusDesc(refundStatusEnum == null ? "unknown" : refundStatusEnum.getDesc());
        return vo;
    }
}
