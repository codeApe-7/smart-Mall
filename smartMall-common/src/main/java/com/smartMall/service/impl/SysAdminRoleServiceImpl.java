package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.SysAdminRole;
import com.smartMall.mapper.SysAdminRoleMapper;
import com.smartMall.service.SysAdminRoleService;
import org.springframework.stereotype.Service;

/**
 * 后台管理员角色 Service 实现。
 */
@Service
public class SysAdminRoleServiceImpl extends ServiceImpl<SysAdminRoleMapper, SysAdminRole> implements SysAdminRoleService {
}
