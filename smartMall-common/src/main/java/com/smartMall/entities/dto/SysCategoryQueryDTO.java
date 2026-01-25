package com.smartMall.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类查询参数DTO
 *
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysCategoryQueryDTO extends PageQueryDTO {

    /**
     * 分类名称（模糊查询）
     */
    private String categoryName;

    /**
     * 父分类ID
     */
    @JsonProperty("pCategoryId")
    private String pCategoryId;

    /**
     * 是否返回树形结构（默认false）
     */
    private Boolean tree = false;
}
