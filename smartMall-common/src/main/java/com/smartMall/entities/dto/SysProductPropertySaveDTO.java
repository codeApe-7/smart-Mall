package com.smartMall.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品属性保存DTO
 *
 * @author 15712
 * @date 2026/1/25
 */
@Data
public class SysProductPropertySaveDTO {

    /**
     * 属性名称
     */
    @NotBlank(message = "属性名称不能为空")
    private String propertyName;

    /**
     * 一级分类ID
     */
    @JsonProperty("pCategoryId")
    private String pCategoryId;

    /**
     * 二级分类ID
     */
    @NotBlank(message = "分类ID不能为空")
    private String categoryId;

    /**
     * 0:无需传封面 1:需传封面
     */
    private Integer coverType = 0;
}
