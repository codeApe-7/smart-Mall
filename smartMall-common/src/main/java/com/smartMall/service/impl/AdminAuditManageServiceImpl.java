package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartMall.entities.domain.AdminOperationLog;
import com.smartMall.entities.dto.AdminOperationLogQueryDTO;
import com.smartMall.entities.vo.AdminOperationLogVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.service.AdminAuditManageService;
import com.smartMall.service.AdminOperationLogService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 后台审计管理 Service 实现。
 */
@Service
public class AdminAuditManageServiceImpl implements AdminAuditManageService {

    @Resource
    private AdminOperationLogService adminOperationLogService;

    @Override
    public PageResultVO<AdminOperationLogVO> loadOperationLogList(AdminOperationLogQueryDTO dto) {
        AdminOperationLogQueryDTO safeQuery = dto == null ? new AdminOperationLogQueryDTO() : dto;
        LambdaQueryWrapper<AdminOperationLog> queryWrapper = new LambdaQueryWrapper<AdminOperationLog>()
                .like(StringTools.isNotEmpty(safeQuery.getAccountName()), AdminOperationLog::getAccountName, safeQuery.getAccountName())
                .eq(StringTools.isNotEmpty(safeQuery.getOperationType()), AdminOperationLog::getOperationType, safeQuery.getOperationType())
                .eq(safeQuery.getOperationStatus() != null, AdminOperationLog::getOperationStatus, safeQuery.getOperationStatus())
                .and(StringTools.isNotEmpty(safeQuery.getKeyword()), wrapper -> wrapper
                        .like(AdminOperationLog::getOperationName, safeQuery.getKeyword())
                        .or()
                        .like(AdminOperationLog::getRequestUri, safeQuery.getKeyword())
                        .or()
                        .like(AdminOperationLog::getErrorMessage, safeQuery.getKeyword()))
                .orderByDesc(AdminOperationLog::getCreateTime);
        Page<AdminOperationLog> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        adminOperationLogService.page(page, queryWrapper);
        return new PageResultVO<>(safeQuery.getPageNo(), safeQuery.getPageSize(), page.getTotal(),
                page.getRecords().stream().map(this::buildVO).toList());
    }

    private AdminOperationLogVO buildVO(AdminOperationLog item) {
        AdminOperationLogVO vo = new AdminOperationLogVO();
        vo.setLogId(item.getLogId());
        vo.setAccountId(item.getAccountId());
        vo.setAccountName(item.getAccountName());
        vo.setOperationType(item.getOperationType());
        vo.setOperationName(item.getOperationName());
        vo.setRequestUri(item.getRequestUri());
        vo.setRequestMethod(item.getRequestMethod());
        vo.setOperationStatus(item.getOperationStatus());
        vo.setErrorMessage(item.getErrorMessage());
        vo.setRequestParam(item.getRequestParam());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }
}
