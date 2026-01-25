package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request DTO
 * 
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/24 23:18
 */
@Data
public class LoginDTO {
    /**
     * Account username or email
     */
    @NotBlank(message = "账号不能为空")
    private String account;
    
    /**
     * Password
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * Check code from user input
     */
    @NotBlank(message = "验证码不能为空")
    private String checkCode;
    
    /**
     * Check code key from Redis
     */
    private String checkCodeKey;
}
