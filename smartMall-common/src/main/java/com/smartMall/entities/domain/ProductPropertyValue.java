package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 商品属性
 * @TableName product_property_value
 */
@TableName(value ="product_property_value")
@Data
public class ProductPropertyValue implements Serializable {
    /**
     * 商品ID
     */
    @TableId(value = "product_id")
    private String productId;

    /**
     * 属性封面
     */
    @TableField(value = "property_value_id")
    private String propertyValueId;

    /**
     * 属性ID
     */
    @TableField(value = "property_id")
    private String propertyId;

    /**
     * 属性名称
     */
    @TableField(value = "property_name")
    private String propertyName;

    /**
     * 属性排序
     */
    @TableField(value = "property_sort")
    private Integer propertySort;

    /**
     * 0:无需封面 1:需封面
     */
    @TableField(value = "cover_type")
    private Integer coverType;

    /**
     * 属性值
     */
    @TableField(value = "property_value")
    private String propertyValue;

    /**
     * 备注
     */
    @TableField(value = "property_remark")
    private String propertyRemark;

    /**
     * 属性值排序
     */
    @TableField(value = "sort")
    private Integer sort;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}