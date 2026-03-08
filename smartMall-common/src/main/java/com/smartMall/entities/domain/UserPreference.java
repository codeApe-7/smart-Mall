package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户偏好档案表。
 */
@Data
@TableName("user_preference")
public class UserPreference implements Serializable {

    @TableId("preference_id")
    private String preferenceId;

    @TableField("user_id")
    private String userId;

    @TableField("favorite_category_ids")
    private String favoriteCategoryIds;

    @TableField("favorite_category_names")
    private String favoriteCategoryNames;

    @TableField("min_price_preference")
    private BigDecimal minPricePreference;

    @TableField("max_price_preference")
    private BigDecimal maxPricePreference;

    @TableField("recent_search_keywords")
    private String recentSearchKeywords;

    @TableField("recent_product_ids")
    private String recentProductIds;

    @TableField("average_rating")
    private BigDecimal averageRating;

    @TableField("preference_tags")
    private String preferenceTags;

    @TableField("order_count")
    private Integer orderCount;

    @TableField("review_count")
    private Integer reviewCount;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
