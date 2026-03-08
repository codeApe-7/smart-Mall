package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Admin dashboard trend item.
 */
@Data
public class AdminDashboardTrendVO implements Serializable {

    private String date;

    private Long count;

    private BigDecimal amount;
}
