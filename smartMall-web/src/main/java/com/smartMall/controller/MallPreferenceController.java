package com.smartMall.controller;

import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.entities.vo.UserPreferenceVO;
import com.smartMall.service.UserPreferenceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User preference controller.
 */
@Slf4j
@RestController
@RequestMapping("/preference")
public class MallPreferenceController {

    @Resource
    private UserPreferenceService userPreferenceService;

    @GetMapping("/profile")
    public ResponseVO<UserPreferenceVO> getProfile(@RequestParam String userId) {
        log.info("web get user preference profile, userId={}", userId);
        return ResponseVO.success(userPreferenceService.getUserPreference(userId));
    }

    @PostMapping("/refresh")
    public ResponseVO<UserPreferenceVO> refresh(@RequestParam String userId) {
        log.info("web refresh user preference, userId={}", userId);
        return ResponseVO.success(userPreferenceService.refreshUserPreference(userId));
    }
}
