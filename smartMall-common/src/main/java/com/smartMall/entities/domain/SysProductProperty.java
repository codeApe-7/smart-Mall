package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 商品属性表
 * @author 15712
 * @TableName sys_product_property
 */
@TableName(value ="sys_product_property")
@Data
public class SysProductProperty implements Serializable {
    /**
     * 属性ID
     */
    @TableId(value = "property_id")
    private String propertyId;

    /**
     * 属性名称
     */
    @TableField(value = "property_name")
    private String propertyName;

    /**
     * 一级分类
     */
    @TableField(value = "p_category_id")
    private String pCategoryId;

    /**
     * 二级分类
     */
    @TableField(value = "category_id")
    private String categoryId;

    /**
     * 排序
     */
    @TableField(value = "property_sort")
    private Integer propertySort;

    /**
     * 0:无需传封面 1:需传封面
     */
    @TableField(value = "cover_type")
    private Integer coverType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}