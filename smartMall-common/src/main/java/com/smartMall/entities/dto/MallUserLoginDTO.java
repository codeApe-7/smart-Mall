package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录参数。
 */
@Data
public class MallUserLoginDTO {

    @NotBlank(message = "account can not be blank")
    private String account;

    @NotBlank(message = "password can not be blank")
    private String password;
}
