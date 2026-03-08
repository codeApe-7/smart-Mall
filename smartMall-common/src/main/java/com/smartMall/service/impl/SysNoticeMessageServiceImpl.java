package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.SysNoticeMessage;
import com.smartMall.mapper.SysNoticeMessageMapper;
import com.smartMall.service.SysNoticeMessageService;
import org.springframework.stereotype.Service;

/**
 * 系统消息通知 Service 实现。
 */
@Service
public class SysNoticeMessageServiceImpl extends ServiceImpl<SysNoticeMessageMapper, SysNoticeMessage>
        implements SysNoticeMessageService {
}
