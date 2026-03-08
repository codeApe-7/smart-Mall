package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册参数。
 */
@Data
public class MallUserRegisterDTO {

    @NotBlank(message = "username can not be blank")
    private String username;

    @NotBlank(message = "password can not be blank")
    private String password;

    @NotBlank(message = "phone can not be blank")
    private String phone;

    private String nickname;
}
