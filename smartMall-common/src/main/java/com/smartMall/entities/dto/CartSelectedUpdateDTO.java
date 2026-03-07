package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车勾选状态更新请求。
 */
@Data
public class CartSelectedUpdateDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotBlank(message = "cartId can not be blank")
    private String cartId;

    @NotNull(message = "selected can not be null")
    private Boolean selected;
}
