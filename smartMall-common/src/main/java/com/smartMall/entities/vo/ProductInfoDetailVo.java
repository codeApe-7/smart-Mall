package com.smartMall.entities.vo;

import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.ProductSku;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2026/2/14 11:29
 */
@Data
public class ProductInfoDetailVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品信息
     */
    private ProductInfo productInfo;

    /**
     * 商品属性列表
     */
    private List<ProductPropertyVO> productPropertyList;

    /**
     * SKU列表
     */
    private List<ProductSkuVO> skuList;
}
