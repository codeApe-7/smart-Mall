package com.smartMall.service;

import com.smartMall.entities.vo.AdminDashboardOverviewVO;

/**
 * Admin dashboard service.
 */
public interface AdminDashboardService {

    /**
     * Load admin dashboard overview.
     *
     * @return dashboard overview
     */
    AdminDashboardOverviewVO getOverview();
}
