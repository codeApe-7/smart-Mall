package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.PaymentInfo;
import com.smartMall.entities.dto.PaymentCallbackDTO;
import com.smartMall.entities.dto.PaymentSubmitDTO;
import com.smartMall.entities.enums.OrderStatusEnum;
import com.smartMall.entities.enums.PaymentChannelEnum;
import com.smartMall.entities.enums.PaymentStatusEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.PaymentSubmitVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.mapper.PaymentInfoMapper;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.PaymentInfoService;
import com.smartMall.service.UserPreferenceRefreshTrigger;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * Payment service implementation.
 */
@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
        implements PaymentInfoService {

    private static final int PAYMENT_NO_SUFFIX_LENGTH = 6;
    private static final DateTimeFormatter PAYMENT_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_CHANNEL = PaymentChannelEnum.ALIPAY_SANDBOX.getCode();
    private static final Set<String> SUCCESS_CALLBACK_STATUS = Set.of("SUCCESS", "TRADE_SUCCESS");
    private static final Set<String> FAILED_CALLBACK_STATUS = Set.of("FAILED");
    private static final Set<String> CLOSED_CALLBACK_STATUS = Set.of("CLOSED", "TRADE_CLOSED");

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private UserPreferenceRefreshTrigger userPreferenceRefreshTrigger;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentSubmitVO submitPayment(PaymentSubmitDTO dto) {
        OrderInfo orderInfo = orderInfoService.getUserOrder(dto.getUserId(), dto.getOrderId());
        if (!OrderStatusEnum.PENDING_PAYMENT.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support payment");
        }
        PaymentInfo paymentInfo = findReusablePayment(orderInfo.getOrderId());
        if (paymentInfo == null) {
            Date now = new Date();
            paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentId(StringTools.getRandomNumber(LENGTH_32));
            paymentInfo.setPaymentNo(generatePaymentNo());
            paymentInfo.setOrderId(orderInfo.getOrderId());
            paymentInfo.setOrderNo(orderInfo.getOrderNo());
            paymentInfo.setUserId(orderInfo.getUserId());
            paymentInfo.setPayChannel(resolvePayChannel(dto.getPayChannel()));
            paymentInfo.setPayStatus(PaymentStatusEnum.PENDING.getStatus());
            paymentInfo.setPayAmount(orderInfo.getTotalAmount());
            paymentInfo.setCreateTime(now);
            paymentInfo.setUpdateTime(now);
            this.save(paymentInfo);
            log.info("create payment record, userId={}, orderId={}, paymentNo={}", dto.getUserId(), dto.getOrderId(), paymentInfo.getPaymentNo());
        } else {
            log.info("reuse pending payment, userId={}, orderId={}, paymentNo={}", dto.getUserId(), dto.getOrderId(), paymentInfo.getPaymentNo());
        }
        return buildSubmitVO(paymentInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(PaymentCallbackDTO dto) {
        PaymentInfo paymentInfo = getPaymentByNo(dto.getPaymentNo());
        String callbackStatus = normalizeCallbackStatus(dto.getCallbackStatus());
        if (SUCCESS_CALLBACK_STATUS.contains(callbackStatus)) {
            handleSuccessCallback(paymentInfo, dto);
            return;
        }
        if (FAILED_CALLBACK_STATUS.contains(callbackStatus)) {
            updatePaymentByCallback(paymentInfo, PaymentStatusEnum.FAILED, dto, null);
            log.info("payment callback marked failed, paymentNo={}, callbackStatus={}", dto.getPaymentNo(), callbackStatus);
            return;
        }
        if (CLOSED_CALLBACK_STATUS.contains(callbackStatus)) {
            updatePaymentByCallback(paymentInfo, PaymentStatusEnum.CLOSED, dto, null);
            log.info("payment callback marked closed, paymentNo={}, callbackStatus={}", dto.getPaymentNo(), callbackStatus);
            return;
        }
        throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "unsupported callbackStatus");
    }

    private void handleSuccessCallback(PaymentInfo paymentInfo, PaymentCallbackDTO dto) {
        if (PaymentStatusEnum.SUCCESS.getStatus().equals(paymentInfo.getPayStatus())) {
            log.info("ignore duplicate success callback, paymentNo={}", paymentInfo.getPaymentNo());
            return;
        }
        OrderInfo orderInfo = orderInfoService.getById(paymentInfo.getOrderId());
        if (orderInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "order not found");
        }
        if (OrderStatusEnum.CANCELED.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order already canceled");
        }
        if (!OrderStatusEnum.PENDING_PAYMENT.getStatus().equals(orderInfo.getOrderStatus())
                && !OrderStatusEnum.PAID.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support payment callback");
        }
        Date payTime = new Date();
        updatePaymentByCallback(paymentInfo, PaymentStatusEnum.SUCCESS, dto, payTime);
        orderInfoService.markOrderPaid(orderInfo.getOrderId(), payTime);
        userPreferenceRefreshTrigger.refreshUserPreferenceAsync(orderInfo.getUserId(), "payment_success");
        log.info("payment callback success, paymentNo={}, orderId={}", paymentInfo.getPaymentNo(), paymentInfo.getOrderId());
    }

    private void updatePaymentByCallback(PaymentInfo paymentInfo,
                                         PaymentStatusEnum paymentStatusEnum,
                                         PaymentCallbackDTO dto,
                                         Date payTime) {
        if (PaymentStatusEnum.SUCCESS.getStatus().equals(paymentInfo.getPayStatus())
                && !PaymentStatusEnum.SUCCESS.equals(paymentStatusEnum)) {
            return;
        }
        Date now = new Date();
        paymentInfo.setPayStatus(paymentStatusEnum.getStatus());
        paymentInfo.setGatewayTradeNo(dto.getGatewayTradeNo());
        paymentInfo.setCallbackContent(buildCallbackContent(dto));
        paymentInfo.setCallbackTime(now);
        paymentInfo.setUpdateTime(now);
        if (payTime != null) {
            paymentInfo.setPayTime(payTime);
        }
        this.updateById(paymentInfo);
    }

    private PaymentInfo getPaymentByNo(String paymentNo) {
        if (StringTools.isEmpty(paymentNo)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "paymentNo is required");
        }
        PaymentInfo paymentInfo = this.getOne(new LambdaQueryWrapper<PaymentInfo>()
                .eq(PaymentInfo::getPaymentNo, paymentNo)
                .last("limit 1"));
        if (paymentInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "payment not found");
        }
        return paymentInfo;
    }

    private PaymentInfo findReusablePayment(String orderId) {
        return this.getOne(new LambdaQueryWrapper<PaymentInfo>()
                .eq(PaymentInfo::getOrderId, orderId)
                .eq(PaymentInfo::getPayStatus, PaymentStatusEnum.PENDING.getStatus())
                .orderByDesc(PaymentInfo::getCreateTime)
                .last("limit 1"));
    }

    private PaymentSubmitVO buildSubmitVO(PaymentInfo paymentInfo) {
        PaymentSubmitVO paymentSubmitVO = new PaymentSubmitVO();
        paymentSubmitVO.setPaymentId(paymentInfo.getPaymentId());
        paymentSubmitVO.setPaymentNo(paymentInfo.getPaymentNo());
        paymentSubmitVO.setOrderId(paymentInfo.getOrderId());
        paymentSubmitVO.setOrderNo(paymentInfo.getOrderNo());
        paymentSubmitVO.setPayChannel(paymentInfo.getPayChannel());
        paymentSubmitVO.setPayChannelDesc(getPayChannelDesc(paymentInfo.getPayChannel()));
        paymentSubmitVO.setPayStatus(paymentInfo.getPayStatus());
        paymentSubmitVO.setPayStatusDesc(getPayStatusDesc(paymentInfo.getPayStatus()));
        paymentSubmitVO.setPayAmount(paymentInfo.getPayAmount());
        paymentSubmitVO.setMockPayUrl("/api/payment/callback");
        paymentSubmitVO.setMockCallbackPayload(buildMockCallbackPayload(paymentInfo));
        return paymentSubmitVO;
    }

    private String buildMockCallbackPayload(PaymentInfo paymentInfo) {
        return String.format("{\"paymentNo\":\"%s\",\"callbackStatus\":\"TRADE_SUCCESS\",\"gatewayTradeNo\":\"%s\"}",
                paymentInfo.getPaymentNo(), "ALI" + paymentInfo.getPaymentNo());
    }

    private String buildCallbackContent(PaymentCallbackDTO dto) {
        if (StringTools.isNotEmpty(dto.getCallbackContent())) {
            return dto.getCallbackContent();
        }
        return String.format("callbackStatus=%s,gatewayTradeNo=%s", dto.getCallbackStatus(), dto.getGatewayTradeNo());
    }

    private String getPayStatusDesc(Integer payStatus) {
        PaymentStatusEnum paymentStatusEnum = PaymentStatusEnum.getByStatus(payStatus);
        return paymentStatusEnum == null ? "unknown" : paymentStatusEnum.getDesc();
    }

    private String getPayChannelDesc(String payChannel) {
        PaymentChannelEnum paymentChannelEnum = PaymentChannelEnum.getByCode(payChannel);
        return paymentChannelEnum == null ? payChannel : paymentChannelEnum.getDesc();
    }

    private String resolvePayChannel(String payChannel) {
        if (StringTools.isEmpty(payChannel)) {
            return DEFAULT_CHANNEL;
        }
        PaymentChannelEnum paymentChannelEnum = PaymentChannelEnum.getByCode(payChannel);
        if (paymentChannelEnum == null) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "unsupported payChannel");
        }
        return paymentChannelEnum.getCode();
    }

    private String normalizeCallbackStatus(String callbackStatus) {
        return callbackStatus == null ? "" : callbackStatus.trim().toUpperCase();
    }

    private String generatePaymentNo() {
        return LocalDateTime.now().format(PAYMENT_NO_TIME_FORMATTER) + StringTools.getRandomNumber(PAYMENT_NO_SUFFIX_LENGTH);
    }
}
