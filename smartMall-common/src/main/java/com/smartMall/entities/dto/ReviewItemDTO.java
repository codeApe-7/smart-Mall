package com.smartMall.entities.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 单条评价项 DTO。
 */
@Data
public class ReviewItemDTO {

    @NotBlank(message = "itemId can not be blank")
    private String itemId;

    @NotBlank(message = "productId can not be blank")
    private String productId;

    @NotNull(message = "rating can not be null")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;

    private String content;
}
