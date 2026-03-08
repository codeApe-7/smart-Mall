package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.ShippingInfo;
import com.smartMall.entities.dto.ConfirmReceiveDTO;
import com.smartMall.entities.dto.ShipOrderDTO;
import com.smartMall.entities.enums.OrderStatusEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.enums.ShippingStatusEnum;
import com.smartMall.entities.vo.ShippingInfoVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.mapper.ShippingInfoMapper;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.ShippingInfoService;
import com.smartMall.service.UserPreferenceRefreshTrigger;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * 物流 Service 实现。
 */
@Service
@Slf4j
public class ShippingInfoServiceImpl extends ServiceImpl<ShippingInfoMapper, ShippingInfo>
        implements ShippingInfoService {

    private static final int TRACKING_NO_SUFFIX_LENGTH = 8;
    private static final DateTimeFormatter TRACKING_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_SHIPPING_COMPANY = "模拟快递";

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private UserPreferenceRefreshTrigger userPreferenceRefreshTrigger;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShippingInfoVO shipOrder(ShipOrderDTO dto) {
        OrderInfo orderInfo = orderInfoService.getUserOrder(dto.getUserId(), dto.getOrderId());
        if (!OrderStatusEnum.PAID.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support shipping");
        }
        ShippingInfo existing = findShippingByOrderId(orderInfo.getOrderId());
        if (existing != null) {
            log.info("reuse existing shipping, orderId={}, trackingNo={}", dto.getOrderId(), existing.getTrackingNo());
            return buildShippingInfoVO(existing);
        }
        Date now = new Date();
        ShippingInfo shippingInfo = new ShippingInfo();
        shippingInfo.setShippingId(StringTools.getRandomNumber(LENGTH_32));
        shippingInfo.setOrderId(orderInfo.getOrderId());
        shippingInfo.setOrderNo(orderInfo.getOrderNo());
        shippingInfo.setUserId(orderInfo.getUserId());
        shippingInfo.setTrackingNo(generateTrackingNo());
        shippingInfo.setShippingCompany(resolveShippingCompany(dto.getShippingCompany()));
        shippingInfo.setShippingStatus(ShippingStatusEnum.SHIPPED.getStatus());
        shippingInfo.setCreateTime(now);
        shippingInfo.setUpdateTime(now);
        this.save(shippingInfo);

        orderInfoService.markOrderShipped(orderInfo.getOrderId(), now);

        log.info("ship order success, orderId={}, trackingNo={}", dto.getOrderId(), shippingInfo.getTrackingNo());
        return buildShippingInfoVO(shippingInfo);
    }

    @Override
    public ShippingInfoVO getShippingDetail(String userId, String orderId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "userId and orderId are required");
        }
        orderInfoService.getUserOrder(userId, orderId);
        ShippingInfo shippingInfo = findShippingByOrderId(orderId);
        if (shippingInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "shipping record not found");
        }
        return buildShippingInfoVO(shippingInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(ConfirmReceiveDTO dto) {
        OrderInfo orderInfo = orderInfoService.getUserOrder(dto.getUserId(), dto.getOrderId());
        if (!OrderStatusEnum.SHIPPED.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support confirming receive");
        }
        ShippingInfo shippingInfo = findShippingByOrderId(orderInfo.getOrderId());
        if (shippingInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "shipping record not found");
        }
        Date now = new Date();
        shippingInfo.setShippingStatus(ShippingStatusEnum.DELIVERED.getStatus());
        shippingInfo.setReceiveTime(now);
        shippingInfo.setUpdateTime(now);
        this.updateById(shippingInfo);

        orderInfoService.markOrderReceived(orderInfo.getOrderId(), now);
        userPreferenceRefreshTrigger.refreshUserPreferenceAsync(dto.getUserId(), "confirm_receive");

        log.info("confirm receive success, orderId={}, trackingNo={}", dto.getOrderId(), shippingInfo.getTrackingNo());
    }

    private ShippingInfo findShippingByOrderId(String orderId) {
        return this.getOne(new LambdaQueryWrapper<ShippingInfo>()
                .eq(ShippingInfo::getOrderId, orderId)
                .orderByDesc(ShippingInfo::getCreateTime)
                .last("limit 1"));
    }

    private ShippingInfoVO buildShippingInfoVO(ShippingInfo shippingInfo) {
        ShippingInfoVO vo = new ShippingInfoVO();
        vo.setShippingId(shippingInfo.getShippingId());
        vo.setOrderId(shippingInfo.getOrderId());
        vo.setOrderNo(shippingInfo.getOrderNo());
        vo.setTrackingNo(shippingInfo.getTrackingNo());
        vo.setShippingCompany(shippingInfo.getShippingCompany());
        vo.setShippingStatus(shippingInfo.getShippingStatus());
        vo.setShippingStatusDesc(getShippingStatusDesc(shippingInfo.getShippingStatus()));
        vo.setCreateTime(shippingInfo.getCreateTime());
        vo.setReceiveTime(shippingInfo.getReceiveTime());
        return vo;
    }

    private String getShippingStatusDesc(Integer shippingStatus) {
        ShippingStatusEnum statusEnum = ShippingStatusEnum.getByStatus(shippingStatus);
        return statusEnum == null ? "unknown" : statusEnum.getDesc();
    }

    private String resolveShippingCompany(String shippingCompany) {
        return StringTools.isEmpty(shippingCompany) ? DEFAULT_SHIPPING_COMPANY : shippingCompany;
    }

    private String generateTrackingNo() {
        return "SF" + LocalDateTime.now().format(TRACKING_NO_TIME_FORMATTER) + StringTools.getRandomNumber(TRACKING_NO_SUFFIX_LENGTH);
    }
}
