package com.smartMall.service;

import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.vo.AssistantChatResponseVO;

/**
 * Assistant agent service based on Spring AI and MCP.
 */
public interface MallAssistantAgentService {

    AssistantChatResponseVO chat(AssistantChatRequestDTO dto);
}
