package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 用户偏好档案视图。
 */
@Data
public class UserPreferenceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String preferenceId;

    private String userId;

    private List<String> favoriteCategoryIds;

    private List<String> favoriteCategoryNames;

    private BigDecimal minPricePreference;

    private BigDecimal maxPricePreference;

    private List<String> recentSearchKeywords;

    private List<String> recentProductIds;

    private BigDecimal averageRating;

    private List<String> preferenceTags;

    private Integer orderCount;

    private Integer reviewCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
