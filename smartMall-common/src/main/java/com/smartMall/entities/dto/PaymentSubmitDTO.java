package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * Payment submit request.
 */
@Data
public class PaymentSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotBlank(message = "orderId can not be blank")
    private String orderId;

    private String payChannel;
}
