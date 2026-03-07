package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * Payment callback request.
 */
@Data
public class PaymentCallbackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "paymentNo can not be blank")
    private String paymentNo;

    @NotBlank(message = "callbackStatus can not be blank")
    private String callbackStatus;

    private String gatewayTradeNo;

    private String callbackContent;
}
