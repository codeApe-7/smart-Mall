package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Payment submit result.
 */
@Data
public class PaymentSubmitVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paymentId;

    private String paymentNo;

    private String orderId;

    private String orderNo;

    private String payChannel;

    private String payChannelDesc;

    private Integer payStatus;

    private String payStatusDesc;

    private BigDecimal payAmount;

    private String mockPayUrl;

    private String mockCallbackPayload;
}
