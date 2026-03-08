package com.smartMall.controller;

import com.smartMall.entities.vo.AdminDashboardOverviewVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.AdminDashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin dashboard controller.
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public ResponseVO<AdminDashboardOverviewVO> overview() {
        return ResponseVO.success(adminDashboardService.getOverview());
    }
}
