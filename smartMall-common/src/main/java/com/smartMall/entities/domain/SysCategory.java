package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author 15712
 * @TableName sys_category
 */
@TableName(value = "sys_category")
@Data
public class SysCategory implements Serializable {
    /**
     * 分类ID
     */
    @TableId(value = "category_id")
    private String categoryId;

    /**
     * 分类名称
     */
    @TableField(value = "category_name")
    private String categoryName;

    /**
     * 父id，默认-1，表示无父级
     */
    @TableField(value = "p_category_id")
    private String pCategoryId;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}