package com.smartMall.entities.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车新增请求。
 */
@Data
public class CartAddDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotBlank(message = "productId can not be blank")
    private String productId;

    @NotBlank(message = "propertyValueIdHash can not be blank")
    private String propertyValueIdHash;

    @NotNull(message = "quantity can not be null")
    @Min(value = 1, message = "quantity must be greater than 0")
    private Integer quantity;
}
