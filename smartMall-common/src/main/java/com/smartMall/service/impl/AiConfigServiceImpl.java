package com.smartMall.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.entities.config.ProductKnowledgeProperties;
import com.smartMall.entities.config.ProductSearchProperties;
import com.smartMall.entities.config.SmartMallAssistantAgentProperties;
import com.smartMall.entities.domain.SysAiConfig;
import com.smartMall.entities.dto.AdminAiConfigSaveDTO;
import com.smartMall.entities.vo.AdminAiConfigVO;
import com.smartMall.mapper.SysAiConfigMapper;
import com.smartMall.service.AiConfigService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * AI config service implementation.
 */
@Service
@Slf4j
public class AiConfigServiceImpl implements AiConfigService {

    private static final String ASSISTANT_CONFIG_CODE = "assistant_agent";
    private static final String PRODUCT_SEARCH_CONFIG_CODE = "product_search";
    private static final String PRODUCT_KNOWLEDGE_CONFIG_CODE = "product_knowledge";

    @Resource
    private SysAiConfigMapper sysAiConfigMapper;

    @Resource
    private SmartMallAssistantAgentProperties assistantAgentProperties;

    @Resource
    private ProductSearchProperties productSearchProperties;

    @Resource
    private ProductKnowledgeProperties productKnowledgeProperties;

    @Override
    public AdminAiConfigVO getAdminAiConfig() {
        AdminAiConfigVO vo = new AdminAiConfigVO();
        vo.setAssistantConfig(buildAssistantConfigVO(getAssistantAgentConfig()));
        vo.setProductSearchConfig(buildProductSearchConfigVO(getProductSearchConfig()));
        vo.setProductKnowledgeConfig(buildProductKnowledgeConfigVO(getProductKnowledgeConfig()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdminAiConfig(AdminAiConfigSaveDTO dto) {
        AdminAiConfigSaveDTO safeDto = dto == null ? new AdminAiConfigSaveDTO() : dto;
        saveConfig(ASSISTANT_CONFIG_CODE, "智能助手配置",
                JSON.toJSONString(normalizeAssistantSnapshot(safeDto.getAssistantConfig(), getAssistantAgentConfig())));
        saveConfig(PRODUCT_SEARCH_CONFIG_CODE, "商品搜索配置",
                JSON.toJSONString(normalizeProductSearchSnapshot(safeDto.getProductSearchConfig(), getProductSearchConfig())));
        saveConfig(PRODUCT_KNOWLEDGE_CONFIG_CODE, "商品知识配置",
                JSON.toJSONString(normalizeProductKnowledgeSnapshot(
                        safeDto.getProductKnowledgeConfig(), getProductKnowledgeConfig())));
    }

    @Override
    public SmartMallAssistantAgentProperties getAssistantAgentConfig() {
        SmartMallAssistantAgentProperties effective = copyAssistantProperties(assistantAgentProperties);
        AssistantConfigSnapshot snapshot = loadConfigSnapshot(ASSISTANT_CONFIG_CODE, AssistantConfigSnapshot.class);
        if (snapshot == null) {
            return effective;
        }
        effective.setEnabled(Boolean.TRUE.equals(snapshot.getEnabled()));
        if (StringTools.isNotEmpty(snapshot.getSystemPrompt())) {
            effective.setSystemPrompt(snapshot.getSystemPrompt().trim());
        }
        return effective;
    }

    @Override
    public ProductSearchProperties getProductSearchConfig() {
        ProductSearchProperties effective = copyProductSearchProperties(productSearchProperties);
        ProductSearchConfigSnapshot snapshot = loadConfigSnapshot(PRODUCT_SEARCH_CONFIG_CODE, ProductSearchConfigSnapshot.class);
        if (snapshot == null) {
            return effective;
        }
        effective.setSemanticEnabled(Boolean.TRUE.equals(snapshot.getSemanticEnabled()));
        if (StringTools.isNotEmpty(snapshot.getElasticsearchUri())) {
            effective.setElasticsearchUri(snapshot.getElasticsearchUri().trim());
        }
        if (StringTools.isNotEmpty(snapshot.getProductIndexName())) {
            effective.setProductIndexName(snapshot.getProductIndexName().trim());
        }
        if (snapshot.getSemanticCandidateSize() != null && snapshot.getSemanticCandidateSize() > 0) {
            effective.setSemanticCandidateSize(snapshot.getSemanticCandidateSize());
        }
        return effective;
    }

    @Override
    public ProductKnowledgeProperties getProductKnowledgeConfig() {
        ProductKnowledgeProperties effective = copyProductKnowledgeProperties(productKnowledgeProperties);
        ProductKnowledgeConfigSnapshot snapshot = loadConfigSnapshot(
                PRODUCT_KNOWLEDGE_CONFIG_CODE, ProductKnowledgeConfigSnapshot.class);
        if (snapshot == null) {
            return effective;
        }
        effective.setEnabled(Boolean.TRUE.equals(snapshot.getEnabled()));
        if (snapshot.getDefaultPageSize() != null && snapshot.getDefaultPageSize() > 0) {
            effective.setDefaultPageSize(snapshot.getDefaultPageSize());
        }
        if (snapshot.getMaxReviewSnippetCount() != null && snapshot.getMaxReviewSnippetCount() > 0) {
            effective.setMaxReviewSnippetCount(snapshot.getMaxReviewSnippetCount());
        }
        if (snapshot.getMaxPropertySnippetCount() != null && snapshot.getMaxPropertySnippetCount() > 0) {
            effective.setMaxPropertySnippetCount(snapshot.getMaxPropertySnippetCount());
        }
        if (snapshot.getMaxCompareCount() != null && snapshot.getMaxCompareCount() > 1) {
            effective.setMaxCompareCount(snapshot.getMaxCompareCount());
        }
        List<String> afterSalesHighlights = normalizeHighlights(snapshot.getAfterSalesHighlights());
        if (!afterSalesHighlights.isEmpty()) {
            effective.setAfterSalesHighlights(afterSalesHighlights);
        }
        return effective;
    }

    private AdminAiConfigVO.AssistantConfig buildAssistantConfigVO(SmartMallAssistantAgentProperties properties) {
        AdminAiConfigVO.AssistantConfig config = new AdminAiConfigVO.AssistantConfig();
        config.setEnabled(properties.isEnabled());
        config.setSystemPrompt(properties.getSystemPrompt());
        return config;
    }

    private AdminAiConfigVO.ProductSearchConfig buildProductSearchConfigVO(ProductSearchProperties properties) {
        AdminAiConfigVO.ProductSearchConfig config = new AdminAiConfigVO.ProductSearchConfig();
        config.setSemanticEnabled(properties.isSemanticEnabled());
        config.setElasticsearchUri(properties.getElasticsearchUri());
        config.setProductIndexName(properties.getProductIndexName());
        config.setSemanticCandidateSize(properties.getSemanticCandidateSize());
        return config;
    }

    private AdminAiConfigVO.ProductKnowledgeConfig buildProductKnowledgeConfigVO(ProductKnowledgeProperties properties) {
        AdminAiConfigVO.ProductKnowledgeConfig config = new AdminAiConfigVO.ProductKnowledgeConfig();
        config.setEnabled(properties.isEnabled());
        config.setDefaultPageSize(properties.getDefaultPageSize());
        config.setMaxReviewSnippetCount(properties.getMaxReviewSnippetCount());
        config.setMaxPropertySnippetCount(properties.getMaxPropertySnippetCount());
        config.setMaxCompareCount(properties.getMaxCompareCount());
        config.setAfterSalesHighlights(copyHighlights(properties.getAfterSalesHighlights()));
        return config;
    }

    private AssistantConfigSnapshot normalizeAssistantSnapshot(AdminAiConfigSaveDTO.AssistantConfig source,
                                                              SmartMallAssistantAgentProperties current) {
        AssistantConfigSnapshot snapshot = new AssistantConfigSnapshot();
        snapshot.setEnabled(source == null || source.getEnabled() == null ? current.isEnabled() : source.getEnabled());
        snapshot.setSystemPrompt(source == null || StringTools.isEmpty(source.getSystemPrompt())
                ? current.getSystemPrompt() : source.getSystemPrompt().trim());
        return snapshot;
    }

    private ProductSearchConfigSnapshot normalizeProductSearchSnapshot(AdminAiConfigSaveDTO.ProductSearchConfig source,
                                                                      ProductSearchProperties current) {
        ProductSearchConfigSnapshot snapshot = new ProductSearchConfigSnapshot();
        snapshot.setSemanticEnabled(source == null || source.getSemanticEnabled() == null
                ? current.isSemanticEnabled() : source.getSemanticEnabled());
        snapshot.setElasticsearchUri(source == null || StringTools.isEmpty(source.getElasticsearchUri())
                ? current.getElasticsearchUri() : source.getElasticsearchUri().trim());
        snapshot.setProductIndexName(source == null || StringTools.isEmpty(source.getProductIndexName())
                ? current.getProductIndexName() : source.getProductIndexName().trim());
        snapshot.setSemanticCandidateSize(source == null || source.getSemanticCandidateSize() == null
                || source.getSemanticCandidateSize() < 1
                ? current.getSemanticCandidateSize() : source.getSemanticCandidateSize());
        return snapshot;
    }

    private ProductKnowledgeConfigSnapshot normalizeProductKnowledgeSnapshot(
            AdminAiConfigSaveDTO.ProductKnowledgeConfig source, ProductKnowledgeProperties current) {
        ProductKnowledgeConfigSnapshot snapshot = new ProductKnowledgeConfigSnapshot();
        snapshot.setEnabled(source == null || source.getEnabled() == null ? current.isEnabled() : source.getEnabled());
        snapshot.setDefaultPageSize(source == null || source.getDefaultPageSize() == null || source.getDefaultPageSize() < 1
                ? current.getDefaultPageSize() : source.getDefaultPageSize());
        snapshot.setMaxReviewSnippetCount(source == null || source.getMaxReviewSnippetCount() == null
                || source.getMaxReviewSnippetCount() < 1
                ? current.getMaxReviewSnippetCount() : source.getMaxReviewSnippetCount());
        snapshot.setMaxPropertySnippetCount(source == null || source.getMaxPropertySnippetCount() == null
                || source.getMaxPropertySnippetCount() < 1
                ? current.getMaxPropertySnippetCount() : source.getMaxPropertySnippetCount());
        snapshot.setMaxCompareCount(source == null || source.getMaxCompareCount() == null || source.getMaxCompareCount() < 2
                ? current.getMaxCompareCount() : source.getMaxCompareCount());
        List<String> highlights = source == null
                ? copyHighlights(current.getAfterSalesHighlights())
                : normalizeHighlights(source.getAfterSalesHighlights());
        snapshot.setAfterSalesHighlights(highlights.isEmpty() ? copyHighlights(current.getAfterSalesHighlights()) : highlights);
        return snapshot;
    }

    private void saveConfig(String configCode, String configName, String configContent) {
        SysAiConfig existing = getConfigByCode(configCode);
        Date now = new Date();
        if (existing == null) {
            SysAiConfig config = new SysAiConfig();
            config.setConfigId(StringTools.getRandomNumber(LENGTH_32));
            config.setConfigCode(configCode);
            config.setConfigName(configName);
            config.setConfigContent(configContent);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            sysAiConfigMapper.insert(config);
            return;
        }
        existing.setConfigName(configName);
        existing.setConfigContent(configContent);
        existing.setUpdateTime(now);
        sysAiConfigMapper.updateById(existing);
    }

    private SysAiConfig getConfigByCode(String configCode) {
        return sysAiConfigMapper.selectOne(new LambdaQueryWrapper<SysAiConfig>()
                .eq(SysAiConfig::getConfigCode, configCode)
                .last("LIMIT 1"));
    }

    private <T> T loadConfigSnapshot(String configCode, Class<T> targetType) {
        SysAiConfig config = getConfigByCode(configCode);
        if (config == null || StringTools.isEmpty(config.getConfigContent())) {
            return null;
        }
        try {
            return JSON.parseObject(config.getConfigContent(), targetType);
        } catch (Exception e) {
            log.warn("parse ai config failed, configCode={}", configCode, e);
            return null;
        }
    }

    private SmartMallAssistantAgentProperties copyAssistantProperties(SmartMallAssistantAgentProperties source) {
        SmartMallAssistantAgentProperties target = new SmartMallAssistantAgentProperties();
        target.setEnabled(source.isEnabled());
        target.setSystemPrompt(source.getSystemPrompt());
        return target;
    }

    private ProductSearchProperties copyProductSearchProperties(ProductSearchProperties source) {
        ProductSearchProperties target = new ProductSearchProperties();
        target.setSemanticEnabled(source.isSemanticEnabled());
        target.setElasticsearchUri(source.getElasticsearchUri());
        target.setProductIndexName(source.getProductIndexName());
        target.setSemanticCandidateSize(source.getSemanticCandidateSize());
        return target;
    }

    private ProductKnowledgeProperties copyProductKnowledgeProperties(ProductKnowledgeProperties source) {
        ProductKnowledgeProperties target = new ProductKnowledgeProperties();
        target.setEnabled(source.isEnabled());
        target.setDefaultPageSize(source.getDefaultPageSize());
        target.setMaxReviewSnippetCount(source.getMaxReviewSnippetCount());
        target.setMaxPropertySnippetCount(source.getMaxPropertySnippetCount());
        target.setMaxCompareCount(source.getMaxCompareCount());
        target.setAfterSalesHighlights(copyHighlights(source.getAfterSalesHighlights()));
        return target;
    }

    private List<String> normalizeHighlights(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringTools::isNotEmpty)
                .distinct()
                .toList();
    }

    private List<String> copyHighlights(List<String> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(source);
    }

    @Data
    private static class AssistantConfigSnapshot {
        private Boolean enabled;
        private String systemPrompt;
    }

    @Data
    private static class ProductSearchConfigSnapshot {
        private Boolean semanticEnabled;
        private String elasticsearchUri;
        private String productIndexName;
        private Integer semanticCandidateSize;
    }

    @Data
    private static class ProductKnowledgeConfigSnapshot {
        private Boolean enabled;
        private Integer defaultPageSize;
        private Integer maxReviewSnippetCount;
        private Integer maxPropertySnippetCount;
        private Integer maxCompareCount;
        private List<String> afterSalesHighlights;
    }
}
