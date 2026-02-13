package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品属性VO（用于前端展示，包含联表查出的分类名称）
 *
 * @author 15712
 * @date 2026/1/25
 */
@Data
public class SysProductPropertyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 属性ID
     */
    private String propertyId;

    /**
     * 属性名称
     */
    private String propertyName;

    /**
     * 一级分类ID
     */
    @JsonProperty("pCategoryId")
    private String pCategoryId;

    /**
     * 二级分类ID
     */
    private String categoryId;

    /**
     * 排序
     */
    private Integer propertySort;

    /**
     * 0:无需传封面 1:需传封面
     */
    private Integer coverType;

    /**
     * 一级分类名称（联表查询）
     */
    @JsonProperty("pCategoryName")
    private String pCategoryName;

    /**
     * 二级分类名称（联表查询）
     */
    private String categoryName;
}
