package com.smartMall.entities.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车数量更新请求。
 */
@Data
public class CartQuantityUpdateDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotBlank(message = "cartId can not be blank")
    private String cartId;

    @NotNull(message = "quantity can not be null")
    @Min(value = 1, message = "quantity must be greater than 0")
    private Integer quantity;
}
