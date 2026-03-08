package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品评价表。
 */
@Data
@TableName("product_review")
public class ProductReview implements Serializable {

    @TableId("review_id")
    private String reviewId;

    @TableField("order_id")
    private String orderId;

    @TableField("item_id")
    private String itemId;

    @TableField("product_id")
    private String productId;

    @TableField("user_id")
    private String userId;

    @TableField("rating")
    private Integer rating;

    @TableField("content")
    private String content;

    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
