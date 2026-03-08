package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Admin review info VO.
 */
@Data
public class AdminReviewInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reviewId;

    private String orderId;

    private String orderNo;

    private String itemId;

    private String productId;

    private String productName;

    private String userId;

    private Integer rating;

    private String content;

    private String replyContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date replyTime;
}
