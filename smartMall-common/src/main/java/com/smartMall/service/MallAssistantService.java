package com.smartMall.service;

import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.dto.AssistantHistoryQueryDTO;
import com.smartMall.entities.vo.AssistantChatHistoryVO;
import com.smartMall.entities.vo.AssistantChatResponseVO;
import com.smartMall.entities.vo.PageResultVO;

/**
 * Intelligent shopping assistant service.
 */
public interface MallAssistantService {

    AssistantChatResponseVO chat(AssistantChatRequestDTO dto);

    PageResultVO<AssistantChatHistoryVO> loadHistory(AssistantHistoryQueryDTO dto);
}
