package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户密码更新参数。
 */
@Data
public class MallUserPasswordUpdateDTO {

    @NotBlank(message = "oldPassword can not be blank")
    private String oldPassword;

    @NotBlank(message = "newPassword can not be blank")
    private String newPassword;
}
