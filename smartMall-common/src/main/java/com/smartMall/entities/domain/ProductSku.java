package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 商品SKU
 * @TableName product_sku
 */
@TableName(value ="product_sku")
@Data
public class ProductSku implements Serializable {
    /**
     * 商品ID
     */
    @TableId(value = "product_id")
    private String productId;

    /**
     * 属性值ID hash
     */
    @TableField(value = "property_value_id_hash")
    private String propertyValueIdHash;

    /**
     * 属性值ID组
     */
    @TableField(value = "property_value_ids")
    private String propertyValueIds;

    /**
     * 价格
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 库存
     */
    @TableField(value = "stock")
    private Integer stock;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}