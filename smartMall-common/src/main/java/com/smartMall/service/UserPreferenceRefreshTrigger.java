package com.smartMall.service;

/**
 * User preference async refresh trigger.
 */
public interface UserPreferenceRefreshTrigger {

    /**
     * Trigger async refresh after user behavior changes.
     *
     * @param userId        user id
     * @param triggerSource trigger source
     */
    void refreshUserPreferenceAsync(String userId, String triggerSource);
}
