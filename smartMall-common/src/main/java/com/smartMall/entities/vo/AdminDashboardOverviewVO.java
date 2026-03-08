package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Admin dashboard overview.
 */
@Data
public class AdminDashboardOverviewVO implements Serializable {

    private AdminDashboardSummaryVO summary;

    private List<AdminDashboardTrendVO> salesTrend;

    private List<AdminDashboardTrendVO> refundTrend;

    private List<AdminPendingShipmentOrderVO> pendingShipmentOrders;

    private List<AdminLowStockProductVO> lowStockProducts;
}
