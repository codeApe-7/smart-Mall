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
public class ProductQueryDTO extends PageQueryDTO {

    /**
     * 产品名称（模糊查询）
     */
    private String productName;

    /**
     * 分类ID或者父分类id
     */
    private String categoryIdOrPCategoryId;

    /**
     * 是否推荐（默认false）
     */
    private Integer commendType;
}
