package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 退款审批 DTO。
 */
@Data
public class RefundAuditDTO {

    @NotBlank(message = "refundId can not be blank")
    private String refundId;

    @NotBlank(message = "userId can not be blank")
    private String userId;
}
