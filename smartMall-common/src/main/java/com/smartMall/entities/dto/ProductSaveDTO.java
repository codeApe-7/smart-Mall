package com.smartMall.entities.dto;

import com.smartMall.entities.vo.ProductInfoVO;
import com.smartMall.entities.vo.ProductPropertyValueVO;
import com.smartMall.entities.vo.ProductSkuVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 商品保存DTO（新增/更新统一使用）
 *
 * @author 15712
 * @date 2026/2/13
 */
@Data
public class ProductSaveDTO {

    /**
     * 商品基本信息
     */
    @NotNull(message = "商品信息不能为空")
    @Valid
    private ProductInfoVO productInfo;

    /**
     * 商品属性值列表
     */
    @Valid
    private List<ProductPropertyValueVO> productPropertyList;

    /**
     * 商品SKU列表
     */
    @Valid
    private List<ProductSkuVO> skuList;
}
