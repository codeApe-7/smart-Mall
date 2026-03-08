package com.smartMall.entities.dto;

import lombok.Data;

import java.util.List;

/**
 * Admin AI config save DTO.
 */
@Data
public class AdminAiConfigSaveDTO {

    private AssistantConfig assistantConfig;

    private ProductSearchConfig productSearchConfig;

    private ProductKnowledgeConfig productKnowledgeConfig;

    @Data
    public static class AssistantConfig {
        private Boolean enabled;
        private String systemPrompt;
    }

    @Data
    public static class ProductSearchConfig {
        private Boolean semanticEnabled;
        private String elasticsearchUri;
        private String productIndexName;
        private Integer semanticCandidateSize;
    }

    @Data
    public static class ProductKnowledgeConfig {
        private Boolean enabled;
        private Integer defaultPageSize;
        private Integer maxReviewSnippetCount;
        private Integer maxPropertySnippetCount;
        private Integer maxCompareCount;
        private List<String> afterSalesHighlights;
    }
}
