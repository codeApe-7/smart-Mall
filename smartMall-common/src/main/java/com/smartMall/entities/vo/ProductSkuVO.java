package com.smartMall.entities.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU VO
 *
 * @author 15712
 * @date 2026/2/13
 */
@Data
public class ProductSkuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 属性值ID hash
     */
    @NotBlank(message = "属性值ID hash不能为空")
    private String propertyValueIdHash;

    /**
     * 属性值ID组
     */
    private String propertyValueIds;

    /**
     * 价格
     */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /**
     * 库存
     */
    @NotNull(message = "库存不能为空")
    private Integer stock;

    /**
     * 排序
     */
    private Integer sort;
}
