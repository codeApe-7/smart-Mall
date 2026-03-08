package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.AdminOperationLog;
import com.smartMall.mapper.AdminOperationLogMapper;
import com.smartMall.service.AdminOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 后台操作审计日志 Service 实现。
 */
@Slf4j
@Service
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog>
        implements AdminOperationLogService {

    @Override
    public void saveSilently(AdminOperationLog operationLog) {
        if (operationLog == null) {
            return;
        }
        try {
            this.save(operationLog);
        } catch (Exception exception) {
            log.warn("save admin audit log failed, operationName={}", operationLog.getOperationName(), exception);
        }
    }
}
