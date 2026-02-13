package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品信息VO
 *
 * @author 15712
 * @date 2026/2/13
 */
@Data
public class ProductInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID（新增时不传，更新时必传）
     */
    private String productId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    /**
     * 商品描述
     */
    private String productDesc;

    /**
     * 封面
     */
    private String cover;

    /**
     * 分类ID
     */
    @NotBlank(message = "分类ID不能为空")
    private String categoryId;

    /**
     * 分类父ID
     */
    @JsonProperty("pCategoryId")
    @NotBlank(message = "父分类ID不能为空")
    private String pCategoryId;
}
