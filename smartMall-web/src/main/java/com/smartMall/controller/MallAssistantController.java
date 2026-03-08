package com.smartMall.controller;

import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.dto.AssistantHistoryQueryDTO;
import com.smartMall.entities.vo.AssistantChatHistoryVO;
import com.smartMall.entities.vo.AssistantChatResponseVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.MallAssistantService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Intelligent shopping assistant controller.
 */
@Slf4j
@RestController
@RequestMapping("/assistant")
public class MallAssistantController {

    @Resource
    private MallAssistantService mallAssistantService;

    @PostMapping("/chat")
    public ResponseVO<AssistantChatResponseVO> chat(@RequestBody @Valid AssistantChatRequestDTO dto) {
        log.info("assistant chat request, userId={}, sessionId={}", dto.getUserId(), dto.getSessionId());
        return ResponseVO.success(mallAssistantService.chat(dto));
    }

    @PostMapping("/history")
    public ResponseVO<PageResultVO<AssistantChatHistoryVO>> history(@RequestBody @Valid AssistantHistoryQueryDTO dto) {
        log.info("assistant history request, userId={}, sessionId={}", dto.getUserId(), dto.getSessionId());
        return ResponseVO.success(mallAssistantService.loadHistory(dto));
    }
}
