package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 退款申请 DTO。
 */
@Data
public class RefundApplyDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotBlank(message = "orderId can not be blank")
    private String orderId;

    private String refundReason;
}
