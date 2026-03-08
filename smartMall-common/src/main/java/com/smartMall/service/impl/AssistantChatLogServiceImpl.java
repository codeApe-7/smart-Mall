package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.AssistantChatLog;
import com.smartMall.mapper.AssistantChatLogMapper;
import com.smartMall.service.AssistantChatLogService;
import org.springframework.stereotype.Service;

/**
 * Assistant chat log service implementation.
 */
@Service
public class AssistantChatLogServiceImpl extends ServiceImpl<AssistantChatLogMapper, AssistantChatLog>
        implements AssistantChatLogService {
}
