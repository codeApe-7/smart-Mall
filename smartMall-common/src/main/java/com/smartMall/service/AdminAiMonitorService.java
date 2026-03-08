package com.smartMall.service;

import com.smartMall.entities.vo.AdminAiMonitorOverviewVO;

/**
 * Admin AI monitor service.
 */
public interface AdminAiMonitorService {

    /**
     * Load AI monitor overview.
     *
     * @return monitor overview
     */
    AdminAiMonitorOverviewVO getOverview();
}
