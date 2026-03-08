package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品评价查询 DTO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewQueryDTO extends PageQueryDTO {

    @NotBlank(message = "productId can not be blank")
    private String productId;
}
