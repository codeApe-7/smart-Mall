package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单明细。
 */
@Data
@TableName("order_item")
public class OrderItem implements Serializable {

    @TableId("item_id")
    private String itemId;

    @TableField("order_id")
    private String orderId;

    @TableField("product_id")
    private String productId;

    @TableField("product_name")
    private String productName;

    @TableField("product_cover")
    private String productCover;

    @TableField("property_value_id_hash")
    private String propertyValueIdHash;

    @TableField("property_value_ids")
    private String propertyValueIds;

    @TableField("sku_property_text")
    private String skuPropertyText;

    @TableField("price")
    private BigDecimal price;

    @TableField("quantity")
    private Integer quantity;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
