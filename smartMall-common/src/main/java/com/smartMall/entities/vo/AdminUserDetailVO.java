package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Admin user detail VO.
 */
@Data
public class AdminUserDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;

    private String username;

    private String nickname;

    private String avatar;

    private String phone;

    private Integer status;

    private String statusDesc;

    private String remark;

    private Long orderCount;

    private BigDecimal totalOrderAmount;

    private Long refundCount;

    private BigDecimal totalRefundAmount;

    private Integer cartItemCount;

    private Integer reviewCount;

    private BigDecimal averageRating;

    private List<String> favoriteCategoryNames;

    private List<String> preferenceTags;

    private List<String> recentSearchKeywords;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastOrderTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastRefundTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastChatTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastActiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
