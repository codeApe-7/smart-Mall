package com.smartMall.service;

import com.smartMall.entities.config.ProductKnowledgeProperties;
import com.smartMall.entities.config.ProductSearchProperties;
import com.smartMall.entities.config.SmartMallAssistantAgentProperties;
import com.smartMall.entities.dto.AdminAiConfigSaveDTO;
import com.smartMall.entities.vo.AdminAiConfigVO;

/**
 * AI config service.
 */
public interface AiConfigService {

    AdminAiConfigVO getAdminAiConfig();

    void saveAdminAiConfig(AdminAiConfigSaveDTO dto);

    SmartMallAssistantAgentProperties getAssistantAgentConfig();

    ProductSearchProperties getProductSearchConfig();

    ProductKnowledgeProperties getProductKnowledgeConfig();
}
