package com.smartMall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.dto.AssistantHistoryQueryDTO;
import com.smartMall.entities.vo.AssistantChatResponseVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.exception.GlobalExceptionHandler;
import com.smartMall.service.MallAssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MallAssistantController API tests.
 */
class MallAssistantControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private MallAssistantService mallAssistantService;

    @BeforeEach
    void setUp() {
        mallAssistantService = mock(MallAssistantService.class);
        MallAssistantController controller = new MallAssistantController();
        ReflectionTestUtils.setField(controller, "mallAssistantService", mallAssistantService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void chatShouldReturnAssistantResponse() throws Exception {
        AssistantChatResponseVO responseVO = new AssistantChatResponseVO();
        responseVO.setIntentType("PRODUCT_SEARCH");
        responseVO.setReply("为你找到商品");
        when(mallAssistantService.chat(any(AssistantChatRequestDTO.class))).thenReturn(responseVO);

        AssistantChatRequestDTO dto = new AssistantChatRequestDTO();
        dto.setUserId("u1");
        dto.setMessage("帮我找手机");

        mockMvc.perform(post("/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.intentType").value("PRODUCT_SEARCH"));
    }

    @Test
    void historyShouldReturnHistoryPage() throws Exception {
        PageResultVO<Object> resultVO = new PageResultVO<>(1, 10, 0L, List.of());
        when(mallAssistantService.loadHistory(any(AssistantHistoryQueryDTO.class))).thenReturn((PageResultVO) resultVO);

        AssistantHistoryQueryDTO dto = new AssistantHistoryQueryDTO();
        dto.setUserId("u1");

        mockMvc.perform(post("/assistant/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pageNo").value(1));
    }
}
