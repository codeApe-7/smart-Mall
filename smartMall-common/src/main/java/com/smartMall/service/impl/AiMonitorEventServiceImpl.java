package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.AiMonitorEvent;
import com.smartMall.mapper.AiMonitorEventMapper;
import com.smartMall.service.AiMonitorEventService;
import com.smartMall.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * AI monitor event service implementation.
 */
@Service
@Slf4j
public class AiMonitorEventServiceImpl extends ServiceImpl<AiMonitorEventMapper, AiMonitorEvent>
        implements AiMonitorEventService {

    @Override
    public void recordEvent(String eventSource, String eventType, String eventCode,
                            String eventMessage, String userId, String sessionId) {
        AiMonitorEvent event = new AiMonitorEvent();
        event.setEventId(StringTools.getRandomNumber(LENGTH_32));
        event.setEventSource(eventSource);
        event.setEventType(eventType);
        event.setEventCode(eventCode);
        event.setEventMessage(eventMessage);
        event.setUserId(userId);
        event.setSessionId(sessionId);
        event.setCreateTime(new Date());
        try {
            this.save(event);
        } catch (Exception e) {
            log.warn("record ai monitor event failed, eventSource={}, eventCode={}", eventSource, eventCode, e);
        }
    }
}
