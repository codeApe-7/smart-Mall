package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 模拟发货 DTO。
 */
@Data
public class ShipOrderDTO {

    @NotBlank(message = "orderId can not be blank")
    private String orderId;

    @NotBlank(message = "userId can not be blank")
    private String userId;

    private String shippingCompany;
}
