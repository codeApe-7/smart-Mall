package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车聚合视图。
 */
@Data
public class ShoppingCartVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ShoppingCartItemVO> items;

    private Integer itemCount;

    private Integer totalQuantity;

    private Integer selectedCount;

    private BigDecimal selectedAmount;
}
