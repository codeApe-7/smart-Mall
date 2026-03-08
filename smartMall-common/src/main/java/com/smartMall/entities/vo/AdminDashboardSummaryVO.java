package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Admin dashboard summary.
 */
@Data
public class AdminDashboardSummaryVO implements Serializable {

    private BigDecimal totalSalesAmount;

    private Long totalOrderCount;

    private Long totalUserCount;

    private BigDecimal totalRefundAmount;

    private Long totalRefundCount;

    private Long pendingRefundCount;

    private Long pendingShipmentCount;
}
