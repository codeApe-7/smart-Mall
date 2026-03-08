package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 确认收货 DTO。
 */
@Data
public class ConfirmReceiveDTO {

    @NotBlank(message = "orderId can not be blank")
    private String orderId;

    @NotBlank(message = "userId can not be blank")
    private String userId;
}
