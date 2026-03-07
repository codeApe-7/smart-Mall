package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单创建结果。
 */
@Data
public class OrderCreateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderId;

    private String orderNo;

    private Integer orderStatus;

    private BigDecimal totalAmount;
}
