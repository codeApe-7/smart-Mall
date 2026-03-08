package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Admin ship order DTO.
 */
@Data
public class AdminShipOrderDTO {

    @NotBlank(message = "orderId can not be blank")
    private String orderId;

    private String shippingCompany;
}
