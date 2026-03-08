package com.smartMall.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.AssistantChatResponseVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.MallAssistantService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Intelligent shopping assistant websocket handler.
 */
@Component
@Slf4j
public class MallAssistantWebSocketHandler extends TextWebSocketHandler {

    @Resource
    private MallAssistantService mallAssistantService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            AssistantChatRequestDTO requestDTO = objectMapper.readValue(message.getPayload(), AssistantChatRequestDTO.class);
            AssistantChatResponseVO responseVO = mallAssistantService.chat(requestDTO);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ResponseVO.success(responseVO))));
        } catch (BusinessException e) {
            log.warn("assistant websocket business error, sessionId={}, message={}", session.getId(), e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ResponseVO.error(e.getCode(), e.getMessage()))));
        } catch (Exception e) {
            log.error("assistant websocket unexpected error, sessionId={}", session.getId(), e);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    ResponseVO.error(ResponseCodeEnum.SYSTEM_ERROR.getCode(), "assistant websocket message parse failed"))));
        }
    }
}
