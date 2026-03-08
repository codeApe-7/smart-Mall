package com.smartMall.entities.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Admin AI monitor properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-mall.admin.ai-monitor")
public class AdminAiMonitorProperties {

    private Integer recentDays = 7;

    private Integer recentEventLimit = 10;

    private String openaiApiKey;

    private String openaiBaseUrl = "https://api.openai.com";

    private String mcpUrl = "http://127.0.0.1:8084/mcp";
}
