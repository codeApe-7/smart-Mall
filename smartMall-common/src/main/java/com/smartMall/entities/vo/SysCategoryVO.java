package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分类VO（用于前端展示）
 *
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/25
 */
@Data
public class SysCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 父分类ID
     */
    @com.fasterxml.jackson.annotation.JsonProperty("pCategoryId")
    private String pCategoryId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 子分类列表（树形结构时使用）
     */
    private List<SysCategoryVO> children;

    /**
     * 商品属性列表（withProperty=true时填充）
     */
    private List<SysProductPropertyVO> properties;
}
