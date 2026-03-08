package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.SysAdminAccountRole;
import com.smartMall.mapper.SysAdminAccountRoleMapper;
import com.smartMall.service.SysAdminAccountRoleService;
import org.springframework.stereotype.Service;

/**
 * 后台管理员账号角色关联 Service 实现。
 */
@Service
public class SysAdminAccountRoleServiceImpl extends ServiceImpl<SysAdminAccountRoleMapper, SysAdminAccountRole>
        implements SysAdminAccountRoleService {
}
