package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.UserAccount;
import com.smartMall.mapper.UserAccountMapper;
import com.smartMall.service.UserAccountService;
import org.springframework.stereotype.Service;

/**
 * 用户账户 Service 实现。
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {
}
