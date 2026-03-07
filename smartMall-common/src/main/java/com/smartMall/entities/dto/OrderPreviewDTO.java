package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 订单预结算请求。
 */
@Data
public class OrderPreviewDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotEmpty(message = "cartIds can not be empty")
    private List<String> cartIds;
}
