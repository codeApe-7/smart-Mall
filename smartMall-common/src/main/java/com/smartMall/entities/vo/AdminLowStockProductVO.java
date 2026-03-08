package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Low stock product card for admin dashboard.
 */
@Data
public class AdminLowStockProductVO implements Serializable {

    private String productId;

    private String productName;

    private String cover;

    private BigDecimal minPrice;

    private Integer totalSale;

    private Integer totalStock;
}
