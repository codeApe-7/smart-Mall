package com.smartMall.service;

import com.smartMall.entities.dto.AdminOperationLogQueryDTO;
import com.smartMall.entities.vo.AdminOperationLogVO;
import com.smartMall.entities.vo.PageResultVO;

/**
 * 后台审计管理 Service。
 */
public interface AdminAuditManageService {

    PageResultVO<AdminOperationLogVO> loadOperationLogList(AdminOperationLogQueryDTO dto);
}
