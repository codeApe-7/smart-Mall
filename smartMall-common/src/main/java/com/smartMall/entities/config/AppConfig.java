package com.smartMall.entities.config;

import org.springframework.context.annotation.Configuration;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/24 22:58
 */
@Configuration("appConfig")
@Data
public class AppConfig {
    @Value("${admin.account}")
    private String adminAccount;
    @Value("${admin.password}")
    private String adminPassword;

}
