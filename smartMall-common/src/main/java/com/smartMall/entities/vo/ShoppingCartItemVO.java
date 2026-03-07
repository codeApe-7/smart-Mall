package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车条目视图。
 */
@Data
public class ShoppingCartItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cartId;

    private String productId;

    private String productName;

    private String productCover;

    private String propertyValueIdHash;

    private String propertyValueIds;

    private String skuPropertyText;

    private BigDecimal price;

    private Integer quantity;

    private Integer stock;

    private Boolean selected;

    private Boolean available;

    private BigDecimal totalAmount;
}
