package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户购物车条目。
 */
@Data
@TableName("shopping_cart")
public class ShoppingCart implements Serializable {

    @TableId("cart_id")
    private String cartId;

    @TableField("user_id")
    private String userId;

    @TableField("product_id")
    private String productId;

    @TableField("property_value_id_hash")
    private String propertyValueIdHash;

    @TableField("property_value_ids")
    private String propertyValueIds;

    @TableField("quantity")
    private Integer quantity;

    @TableField("selected")
    private Integer selected;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
