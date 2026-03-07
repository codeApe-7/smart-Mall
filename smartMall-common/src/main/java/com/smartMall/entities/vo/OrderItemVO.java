package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细视图。
 */
@Data
public class OrderItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String itemId;

    private String productId;

    private String productName;

    private String productCover;

    private String propertyValueIdHash;

    private String propertyValueIds;

    private String skuPropertyText;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalAmount;
}
