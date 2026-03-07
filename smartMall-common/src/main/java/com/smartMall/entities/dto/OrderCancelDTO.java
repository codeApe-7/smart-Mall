package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 取消订单请求。
 */
@Data
public class OrderCancelDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotBlank(message = "orderId can not be blank")
    private String orderId;
}
