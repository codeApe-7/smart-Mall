package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单预结算视图。
 */
@Data
public class OrderPreviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<OrderItemVO> items;

    private Integer totalQuantity;

    private BigDecimal totalAmount;
}
