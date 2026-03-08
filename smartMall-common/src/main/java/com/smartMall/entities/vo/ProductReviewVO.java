package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品评价视图。
 */
@Data
public class ProductReviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reviewId;

    private String orderId;

    private String itemId;

    private String productId;

    private String productName;

    private String userId;

    private Integer rating;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
