package com.smartMall.entities.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Admin dashboard properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-mall.admin.dashboard")
public class AdminDashboardProperties {

    private int trendDays = 7;

    private int lowStockThreshold = 10;

    private int lowStockLimit = 10;

    private int pendingShipmentLimit = 10;
}
