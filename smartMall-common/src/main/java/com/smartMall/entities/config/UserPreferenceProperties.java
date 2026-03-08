package com.smartMall.entities.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * User preference properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-mall.preference")
public class UserPreferenceProperties {

    private int recentOrderDays = 90;

    private int topCategoryCount = 5;

    private int recentSearchKeywordCount = 10;

    private int defaultRecommendLimit = 6;
}
