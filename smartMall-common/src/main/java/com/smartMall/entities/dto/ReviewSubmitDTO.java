package com.smartMall.entities.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 提交评价 DTO。
 */
@Data
public class ReviewSubmitDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotBlank(message = "orderId can not be blank")
    private String orderId;

    @NotEmpty(message = "reviews can not be empty")
    @Valid
    private List<ReviewItemDTO> reviews;
}
