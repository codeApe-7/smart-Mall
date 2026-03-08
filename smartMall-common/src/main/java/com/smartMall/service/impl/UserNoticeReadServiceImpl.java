package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.UserNoticeRead;
import com.smartMall.mapper.UserNoticeReadMapper;
import com.smartMall.service.UserNoticeReadService;
import org.springframework.stereotype.Service;

/**
 * 用户消息已读记录 Service 实现。
 */
@Service
public class UserNoticeReadServiceImpl extends ServiceImpl<UserNoticeReadMapper, UserNoticeRead>
        implements UserNoticeReadService {
}
