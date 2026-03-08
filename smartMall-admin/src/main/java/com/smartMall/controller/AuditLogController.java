package com.smartMall.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.smartMall.entities.dto.AdminOperationLogQueryDTO;
import com.smartMall.entities.vo.AdminOperationLogVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.AdminAuditManageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台审计日志控制器。
 */
@RestController
@RequestMapping("/audit")
@SaCheckPermission("audit:log")
public class AuditLogController {

    @Resource
    private AdminAuditManageService adminAuditManageService;

    @PostMapping("/list")
    public ResponseVO<PageResultVO<AdminOperationLogVO>> list(@RequestBody(required = false) AdminOperationLogQueryDTO dto) {
        return ResponseVO.success(adminAuditManageService.loadOperationLogList(dto));
    }
}


