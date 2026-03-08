package com.smartMall.controller;

import com.smartMall.entities.vo.AdminAiMonitorOverviewVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.AdminAiMonitorService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin AI monitor controller.
 */
@RestController
@RequestMapping("/ai-monitor")
public class AiMonitorController {

    @Resource
    private AdminAiMonitorService adminAiMonitorService;

    @GetMapping("/overview")
    public ResponseVO<AdminAiMonitorOverviewVO> overview() {
        return ResponseVO.success(adminAiMonitorService.getOverview());
    }
}
