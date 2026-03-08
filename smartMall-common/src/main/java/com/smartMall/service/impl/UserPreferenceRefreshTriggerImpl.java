package com.smartMall.service.impl;

import com.smartMall.service.UserPreferenceRefreshTrigger;
import com.smartMall.service.UserPreferenceService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * User preference async refresh trigger implementation.
 */
@Service
@Slf4j
public class UserPreferenceRefreshTriggerImpl implements UserPreferenceRefreshTrigger {

    @Resource
    private UserPreferenceService userPreferenceService;

    @Override
    @Async("userPreferenceRefreshExecutor")
    public void refreshUserPreferenceAsync(String userId, String triggerSource) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        try {
            userPreferenceService.refreshUserPreference(userId);
            log.info("async refresh user preference success, userId={}, triggerSource={}", userId, triggerSource);
        } catch (Exception e) {
            log.warn("async refresh user preference failed, userId={}, triggerSource={}", userId, triggerSource, e);
        }
    }
}
