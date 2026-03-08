package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户收货地址保存参数。
 */
@Data
public class UserAddressSaveDTO {

    private String addressId;

    @NotBlank(message = "receiverName can not be blank")
    private String receiverName;

    @NotBlank(message = "receiverPhone can not be blank")
    private String receiverPhone;

    @NotBlank(message = "province can not be blank")
    private String province;

    @NotBlank(message = "city can not be blank")
    private String city;

    private String region;

    @NotBlank(message = "detailAddress can not be blank")
    private String detailAddress;

    private String addressLabel;

    private Boolean defaultAddress;
}
