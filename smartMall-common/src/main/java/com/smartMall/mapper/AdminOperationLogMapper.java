package com.smartMall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartMall.entities.domain.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台操作审计日志 Mapper。
 */
@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}
