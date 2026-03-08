package com.smartMall.service.impl;

import com.smartMall.config.SmartMallAssistantAgentProperties;
import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.vo.AssistantChatResponseVO;
import com.smartMall.service.MallAssistantAgentService;
import com.smartMall.service.MallAssistantService;
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
        promptBuilder.append("请根据上下文直接帮助用户完成商城操作。");
        return promptBuilder.toString();
    }
}
