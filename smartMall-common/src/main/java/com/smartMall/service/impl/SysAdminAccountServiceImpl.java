package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.SysAdminAccount;
import com.smartMall.mapper.SysAdminAccountMapper;
import com.smartMall.service.SysAdminAccountService;
import org.springframework.stereotype.Service;

/**
 * 后台管理员账号 Service 实现。
 */
@Service
public class SysAdminAccountServiceImpl extends ServiceImpl<SysAdminAccountMapper, SysAdminAccount>
        implements SysAdminAccountService {
}
