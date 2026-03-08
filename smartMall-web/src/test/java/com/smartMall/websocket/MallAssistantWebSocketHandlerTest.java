package com.smartMall.websocket;

import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.vo.AssistantChatResponseVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.MallAssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MallAssistantWebSocketHandler tests.
 */
class MallAssistantWebSocketHandlerTest {

    private MallAssistantWebSocketHandler handler;

    private MallAssistantService mallAssistantService;

    @BeforeEach
    void setUp() {
        handler = new MallAssistantWebSocketHandler();
        mallAssistantService = mock(MallAssistantService.class);
        ReflectionTestUtils.setField(handler, "mallAssistantService", mallAssistantService);
    }

    @Test
    void handleTextMessageShouldReturnSuccessResponse() throws Exception {
        AssistantChatResponseVO responseVO = new AssistantChatResponseVO();
        responseVO.setIntentType("PRODUCT_RECOMMEND");
        responseVO.setReply("推荐商品");
        when(mallAssistantService.chat(any(AssistantChatRequestDTO.class))).thenReturn(responseVO);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("ws-1");

        handler.handleMessage(session, new TextMessage("{\"userId\":\"u1\",\"message\":\"帮我推荐商品\"}"));

        verify(session).sendMessage(argThat((TextMessage message) ->
                message.getPayload().contains("\"code\":200")
                        && message.getPayload().contains("PRODUCT_RECOMMEND")));
    }

    @Test
    void handleTextMessageShouldReturnBusinessError() throws Exception {
        doThrow(new BusinessException(501, "order can not be canceled"))
                .when(mallAssistantService).chat(any(AssistantChatRequestDTO.class));

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("ws-2");

        handler.handleMessage(session, new TextMessage("{\"userId\":\"u1\",\"message\":\"取消订单 o1\"}"));

        verify(session).sendMessage(argThat((TextMessage message) ->
                message.getPayload().contains("\"code\":501")
                        && message.getPayload().contains("order can not be canceled")));
    }
}
