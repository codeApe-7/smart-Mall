package com.smartMall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2026/1/20 21:27
 */
@SpringBootApplication(scanBasePackages = "com.smartMall",exclude = {DataSourceAutoConfiguration.class})
public class SmartMallWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartMallWebApplication.class, args);
    }
}
