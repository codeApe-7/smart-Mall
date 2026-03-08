package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.AiMonitorEvent;

/**
 * AI monitor event service.
 */
public interface AiMonitorEventService extends IService<AiMonitorEvent> {

    /**
     * Record one AI monitor event.
     *
     * @param eventSource event source
     * @param eventType event type
     * @param eventCode event code
     * @param eventMessage event message
     * @param userId user id
     * @param sessionId session id
     */
    void recordEvent(String eventSource, String eventType, String eventCode,
                     String eventMessage, String userId, String sessionId);
}
