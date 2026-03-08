package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.AdminOperationLog;

/**
 * 后台操作审计日志 Service。
 */
public interface AdminOperationLogService extends IService<AdminOperationLog> {

    void saveSilently(AdminOperationLog log);
}
