package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 购物车删除请求。
 */
@Data
public class CartDeleteDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotEmpty(message = "cartIds can not be empty")
    private List<String> cartIds;
}
