package com.smartMall.service.impl;

import com.smartMall.config.SmartMallAssistantAgentProperties;
import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.dto.ProductKnowledgeCompareDTO;
import com.smartMall.entities.dto.ProductKnowledgeQueryDTO;
import com.smartMall.entities.vo.ProductKnowledgeCompareVO;
import com.smartMall.entities.vo.AssistantChatResponseVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductKnowledgeVO;
import com.smartMall.entities.vo.UserPreferenceVO;
import com.smartMall.service.MallAssistantAgentService;
import com.smartMall.service.MallAssistantService;
import com.smartMall.service.ProductKnowledgeService;
import com.smartMall.service.UserPreferenceService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * Assistant agent implementation based on Spring AI.
 */
@Service
@Slf4j
public class MallAssistantAgentServiceImpl implements MallAssistantAgentService {

    private static final String AI_AGENT_INTENT = "AI_AGENT";
    private static final String AI_AGENT_DESC = "spring ai agent";
    private static final List<String> COMPARE_KEYWORDS = List.of("对比", "比较", "哪个好", "怎么选", "区别", "vs", "VS", "pk", "PK");
    private static final List<String> DEFAULT_SUGGESTIONS = List.of(
            "建议：继续告诉我你想买什么类型的商品",
            "建议：直接输入订单号查询订单详情",
            "建议：让我帮你推荐几款热销商品");

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ObjectProvider<ToolCallbackProvider> toolCallbackProvider;

    @Resource
    private MallAssistantService mallAssistantService;

    @Resource
    private SmartMallAssistantAgentProperties properties;

    @Resource
    private ProductKnowledgeService productKnowledgeService;

    @Resource
    private UserPreferenceService userPreferenceService;

    public MallAssistantAgentServiceImpl(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                                         ObjectProvider<ToolCallbackProvider> toolCallbackProvider) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @Override
    public AssistantChatResponseVO chat(AssistantChatRequestDTO dto) {
        AssistantChatRequestDTO requestDTO = copyRequest(dto);
        requestDTO.setSessionId(normalizeSessionId(dto.getSessionId()));

        if (!properties.isEnabled()) {
            log.info("assistant agent disabled, fallback to rule flow, userId={}", dto.getUserId());
            return mallAssistantService.chat(requestDTO);
        }

        ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();
        ToolCallbackProvider callbackProvider = toolCallbackProvider.getIfAvailable();
        if (chatClientBuilder == null || callbackProvider == null || callbackProvider.getToolCallbacks().length == 0) {
            log.info("assistant agent dependencies unavailable, fallback to rule flow, userId={}", dto.getUserId());
            return mallAssistantService.chat(requestDTO);
        }

        try {
            String reply = chatClientBuilder
                    .defaultSystem(properties.getSystemPrompt())
                    .build()
                    .prompt()
                    .user(buildUserPrompt(requestDTO))
                    .toolCallbacks(callbackProvider)
                    .call()
                    .content();
            AssistantChatResponseVO response = buildAgentResponse(requestDTO.getSessionId(), reply);
            mallAssistantService.recordChat(requestDTO, response);
            log.info("assistant agent handled message by spring ai, userId={}, sessionId={}",
                    requestDTO.getUserId(), requestDTO.getSessionId());
            return response;
        } catch (Exception e) {
            log.warn("assistant agent invoke failed, fallback to rule flow, userId={}, sessionId={}",
                    requestDTO.getUserId(), requestDTO.getSessionId(), e);
            return mallAssistantService.chat(requestDTO);
        }
    }

    private AssistantChatResponseVO buildAgentResponse(String sessionId, String reply) {
        AssistantChatResponseVO response = new AssistantChatResponseVO();
        response.setSessionId(sessionId);
        response.setIntentType(AI_AGENT_INTENT);
        response.setIntentDesc(AI_AGENT_DESC);
        response.setReply(StringTools.isEmpty(reply) ? "我已经处理你的请求，但暂时没有生成可展示的回复。" : reply.trim());
        response.setSuggestions(DEFAULT_SUGGESTIONS);
        response.setResponseTime(new Date());
        return response;
    }

    private AssistantChatRequestDTO copyRequest(AssistantChatRequestDTO dto) {
        AssistantChatRequestDTO requestDTO = new AssistantChatRequestDTO();
        requestDTO.setUserId(dto.getUserId());
        requestDTO.setSessionId(dto.getSessionId());
        requestDTO.setMessage(dto.getMessage());
        requestDTO.setProductId(dto.getProductId());
        requestDTO.setOrderId(dto.getOrderId());
        requestDTO.setRefundReason(dto.getRefundReason());
        requestDTO.setReviews(dto.getReviews());
        return requestDTO;
    }

    private String normalizeSessionId(String sessionId) {
        return StringTools.isEmpty(sessionId) ? StringTools.getRandomNumber(LENGTH_32) : sessionId;
    }

    private String buildUserPrompt(AssistantChatRequestDTO dto) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("用户ID：").append(dto.getUserId()).append('\n');
        promptBuilder.append("会话ID：").append(dto.getSessionId()).append('\n');
        promptBuilder.append("用户消息：").append(dto.getMessage()).append('\n');
        if (StringTools.isNotEmpty(dto.getProductId())) {
            promptBuilder.append("商品ID：").append(dto.getProductId()).append('\n');
        }
        if (StringTools.isNotEmpty(dto.getOrderId())) {
            promptBuilder.append("订单ID：").append(dto.getOrderId()).append('\n');
        }
        if (StringTools.isNotEmpty(dto.getRefundReason())) {
            promptBuilder.append("退款原因：").append(dto.getRefundReason()).append('\n');
        }
        if (dto.getReviews() != null && !dto.getReviews().isEmpty()) {
            promptBuilder.append("评价条目数量：").append(dto.getReviews().size()).append('\n');
            promptBuilder.append("如果用户要求提交评价，请根据上下文判断并尽量利用可用工具先查询订单信息。").append('\n');
        }
        String preferenceContext = buildPreferenceContext(dto.getUserId());
        if (StringTools.isNotEmpty(preferenceContext)) {
            promptBuilder.append("用户偏好上下文：").append('\n');
            promptBuilder.append(preferenceContext).append('\n');
        }
        String knowledgeContext = buildKnowledgeContext(dto);
        if (StringTools.isNotEmpty(knowledgeContext)) {
            promptBuilder.append("商品知识增强上下文：").append('\n');
            promptBuilder.append(knowledgeContext).append('\n');
        }
        promptBuilder.append("请根据上下文直接帮助用户完成商城操作。");
        return promptBuilder.toString();
    }

    private String buildPreferenceContext(String userId) {
        if (StringTools.isEmpty(userId)) {
            return "";
        }
        try {
            UserPreferenceVO preference = userPreferenceService.getUserPreference(userId);
            if (preference == null || preference.getPreferenceId() == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            if (preference.getFavoriteCategoryNames() != null && !preference.getFavoriteCategoryNames().isEmpty()) {
                sb.append("偏好分类：").append(String.join("、", preference.getFavoriteCategoryNames())).append('\n');
            }
            if (preference.getMinPricePreference() != null && preference.getMaxPricePreference() != null) {
                sb.append("价格区间：").append(preference.getMinPricePreference())
                        .append("~").append(preference.getMaxPricePreference()).append("元").append('\n');
            }
            if (preference.getPreferenceTags() != null && !preference.getPreferenceTags().isEmpty()) {
                sb.append("偏好标签：").append(String.join("、", preference.getPreferenceTags())).append('\n');
            }
            if (preference.getOrderCount() != null && preference.getOrderCount() > 0) {
                sb.append("历史订单数：").append(preference.getOrderCount()).append('\n');
            }
            if (preference.getAverageRating() != null) {
                sb.append("用户平均评分：").append(preference.getAverageRating()).append('\n');
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("load user preference context failed, userId={}", userId, e);
            return "";
        }
    }

    private String buildKnowledgeContext(AssistantChatRequestDTO dto) {
        try {
            String comparisonContext = buildComparisonContext(dto);
            if (StringTools.isNotEmpty(comparisonContext)) {
                return comparisonContext;
            }
            if (StringTools.isNotEmpty(dto.getProductId())) {
                ProductKnowledgeVO knowledgeVO = productKnowledgeService.getKnowledgeDetail(dto.getProductId());
                return knowledgeVO.getKnowledgeText();
            }
            if (StringTools.isEmpty(dto.getMessage()) || StringTools.isNotEmpty(dto.getOrderId())) {
                return "";
            }
            ProductKnowledgeQueryDTO queryDTO = new ProductKnowledgeQueryDTO();
            queryDTO.setKeyword(dto.getMessage());
            queryDTO.setPageNo(1);
            queryDTO.setPageSize(2);
            PageResultVO<ProductKnowledgeVO> knowledgePage = productKnowledgeService.searchKnowledge(queryDTO);
            if (knowledgePage.getRecords() == null || knowledgePage.getRecords().isEmpty()) {
                return "";
            }
            return knowledgePage.getRecords().stream()
                    .map(ProductKnowledgeVO::getKnowledgeText)
                    .reduce((left, right) -> left + "\n---\n" + right)
                    .orElse("");
        } catch (Exception e) {
            log.warn("load product knowledge context failed, userId={}, sessionId={}", dto.getUserId(), dto.getSessionId(), e);
            return "";
        }
    }

    private String buildComparisonContext(AssistantChatRequestDTO dto) {
        if (StringTools.isEmpty(dto.getMessage()) || StringTools.isNotEmpty(dto.getProductId())
                || StringTools.isNotEmpty(dto.getOrderId()) || !requiresComparisonContext(dto.getMessage())) {
            return "";
        }
        ProductKnowledgeCompareDTO compareDTO = new ProductKnowledgeCompareDTO();
        compareDTO.setKeyword(dto.getMessage());
        compareDTO.setMaxCount(3);
        ProductKnowledgeCompareVO compareVO = productKnowledgeService.compareKnowledge(compareDTO);
        if (!Boolean.TRUE.equals(compareVO.getComparable())) {
            return "";
        }
        return compareVO.getComparisonText();
    }

    private boolean requiresComparisonContext(String message) {
        return COMPARE_KEYWORDS.stream().anyMatch(message::contains);
    }
}
