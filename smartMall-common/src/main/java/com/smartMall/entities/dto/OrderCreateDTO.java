package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求。
 */
@Data
public class OrderCreateDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotEmpty(message = "cartIds can not be empty")
    private List<String> cartIds;

    @NotBlank(message = "receiverName can not be blank")
    private String receiverName;

    @NotBlank(message = "receiverPhone can not be blank")
    private String receiverPhone;

    @NotBlank(message = "receiverAddress can not be blank")
    private String receiverAddress;

    private String orderRemark;
}
