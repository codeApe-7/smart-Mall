package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.entities.config.AdminAiMonitorProperties;
import com.smartMall.entities.config.SmartMallAssistantAgentProperties;
import com.smartMall.entities.domain.AiMonitorEvent;
import com.smartMall.entities.domain.AssistantChatLog;
import com.smartMall.entities.vo.AdminAiMonitorMetricVO;
import com.smartMall.entities.vo.AdminAiMonitorOverviewVO;
import com.smartMall.entities.vo.AdminAiMonitorRecentEventVO;
import com.smartMall.entities.vo.AdminAiMonitorTrendVO;
import com.smartMall.entities.vo.AdminAiServiceStatusVO;
import com.smartMall.service.AdminAiMonitorService;
import com.smartMall.service.AdminKnowledgeManageService;
import com.smartMall.service.AiConfigService;
import com.smartMall.service.AiMonitorEventService;
import com.smartMall.service.AssistantChatLogService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Admin AI monitor service implementation.
 */
@Service
public class AdminAiMonitorServiceImpl implements AdminAiMonitorService {

    private static final String AI_AGENT_INTENT = "AI_AGENT";

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    @Resource
    private AdminAiMonitorProperties adminAiMonitorProperties;

    @Resource
    private AiConfigService aiConfigService;

    @Resource
    private AssistantChatLogService assistantChatLogService;

    @Resource
    private AiMonitorEventService aiMonitorEventService;

    @Resource
    private AdminKnowledgeManageService adminKnowledgeManageService;

    @Override
    public AdminAiMonitorOverviewVO getOverview() {
        int recentDays = Math.max(1, adminAiMonitorProperties.getRecentDays());
        Date recentSince = toDate(LocalDate.now().minusDays(recentDays - 1L));
        Date todayStart = toDate(LocalDate.now());

        List<AssistantChatLog> chatLogs = assistantChatLogService.list(new LambdaQueryWrapper<AssistantChatLog>()
                .orderByDesc(AssistantChatLog::getCreateTime));
        List<AiMonitorEvent> recentEvents = aiMonitorEventService.list(new LambdaQueryWrapper<AiMonitorEvent>()
                .ge(AiMonitorEvent::getCreateTime, recentSince)
                .orderByDesc(AiMonitorEvent::getCreateTime)
                .last("LIMIT " + Math.max(1, adminAiMonitorProperties.getRecentEventLimit())));

        AdminAiMonitorOverviewVO overviewVO = new AdminAiMonitorOverviewVO();
        overviewVO.setRecentDays(recentDays);
        overviewVO.setTotalChatCount((long) chatLogs.size());
        overviewVO.setAiAgentChatCount(chatLogs.stream()
                .filter(item -> Objects.equals(item.getIntentType(), AI_AGENT_INTENT))
                .count());
        overviewVO.setRuleChatCount(overviewVO.getTotalChatCount() - overviewVO.getAiAgentChatCount());
        overviewVO.setTodayChatCount(chatLogs.stream()
                .filter(item -> item.getCreateTime() != null && !item.getCreateTime().before(todayStart))
                .count());
        overviewVO.setTodayActiveUserCount(chatLogs.stream()
                .filter(item -> item.getCreateTime() != null && !item.getCreateTime().before(todayStart))
                .map(AssistantChatLog::getUserId)
                .filter(StringTools::isNotEmpty)
                .distinct()
                .count());
        overviewVO.setLastChatTime(chatLogs.isEmpty() ? null : chatLogs.getFirst().getCreateTime());
        overviewVO.setKnowledgeIndexSummary(adminKnowledgeManageService.getIndexSummary());
        overviewVO.setServiceStatuses(buildServiceStatuses(overviewVO.getKnowledgeIndexSummary()));
        overviewVO.setFallbackStats(buildFallbackStats(recentSince));
        overviewVO.setErrorCodeStats(buildErrorCodeStats(recentSince));
        overviewVO.setToolInvokeStats(buildToolInvokeStats(recentSince));
        overviewVO.setDailyTrends(buildDailyTrends(chatLogs, recentSince));
        overviewVO.setRecentEvents(recentEvents.stream().map(this::buildRecentEventVO).toList());
        return overviewVO;
    }

    private List<AdminAiServiceStatusVO> buildServiceStatuses(
            com.smartMall.entities.vo.AdminKnowledgeIndexSummaryVO knowledgeIndexSummary) {
        SmartMallAssistantAgentProperties assistantConfig = aiConfigService.getAssistantAgentConfig();
        boolean assistantEnabled = assistantConfig.isEnabled();
        boolean openAiConfigured = StringTools.isNotEmpty(adminAiMonitorProperties.getOpenaiApiKey());
        boolean openAiReachable = openAiConfigured && checkHttpReachable(adminAiMonitorProperties.getOpenaiBaseUrl());
        boolean mcpReachable = checkHttpReachable(adminAiMonitorProperties.getMcpUrl());

        return List.of(
                buildStatus("assistant-agent", "智能助手", assistantEnabled ? "UP" : "DISABLED",
                        assistantEnabled ? "智能助手已开启" : "智能助手已关闭"),
                buildStatus("openai-model", "模型连通性",
                        assistantEnabled ? (openAiConfigured && openAiReachable ? "UP" : "DOWN") : "DISABLED",
                        assistantEnabled
                                ? (openAiConfigured
                                ? (openAiReachable ? "模型基础地址可达" : "模型基础地址不可达")
                                : "未配置 OPENAI_API_KEY")
                                : "智能助手关闭时不校验模型"),
                buildStatus("mcp-tools", "MCP 工具连通性",
                        assistantEnabled ? (mcpReachable ? "UP" : "DOWN") : "DISABLED",
                        assistantEnabled
                                ? (mcpReachable ? "MCP 服务可达" : "MCP 服务不可达")
                                : "智能助手关闭时不校验 MCP"),
                buildStatus("semantic-search", "语义搜索",
                        Boolean.TRUE.equals(knowledgeIndexSummary.getSemanticSearchEnabled())
                                ? (Boolean.TRUE.equals(knowledgeIndexSummary.getReachable()) ? "UP" : "DOWN")
                                : "DISABLED",
                        Boolean.TRUE.equals(knowledgeIndexSummary.getSemanticSearchEnabled())
                                ? (Boolean.TRUE.equals(knowledgeIndexSummary.getReachable())
                                ? "Elasticsearch 可达"
                                : "Elasticsearch 不可达")
                                : "语义搜索未开启"),
                buildStatus("rag-product", "商品知识增强",
                        Boolean.TRUE.equals(knowledgeIndexSummary.getProductKnowledgeEnabled()) ? "UP" : "DISABLED",
                        Boolean.TRUE.equals(knowledgeIndexSummary.getProductKnowledgeEnabled())
                                ? "商品知识增强已开启" : "商品知识增强未开启")
        );
    }

    private List<AdminAiMonitorMetricVO> buildFallbackStats(Date recentSince) {
        List<AiMonitorEvent> fallbackEvents = loadEventsByType(recentSince, "FALLBACK");
        Map<String, Long> countMap = fallbackEvents.stream()
                .collect(Collectors.groupingBy(AiMonitorEvent::getEventCode, Collectors.counting()));
        return countMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> buildMetric(entry.getKey(), formatMetricName(entry.getKey()), entry.getValue()))
                .toList();
    }

    private List<AdminAiMonitorMetricVO> buildErrorCodeStats(Date recentSince) {
        return loadEventsByType(recentSince, "FALLBACK").stream()
                .collect(Collectors.groupingBy(AiMonitorEvent::getEventCode, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> buildMetric(entry.getKey(), formatMetricName(entry.getKey()), entry.getValue()))
                .toList();
    }

    private List<AdminAiMonitorMetricVO> buildToolInvokeStats(Date recentSince) {
        return aiMonitorEventService.list(new LambdaQueryWrapper<AiMonitorEvent>()
                        .ge(AiMonitorEvent::getCreateTime, recentSince)
                        .orderByDesc(AiMonitorEvent::getCreateTime)).stream()
                .collect(Collectors.groupingBy(item -> item.getEventSource() + ":" + item.getEventType(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> buildMetric(entry.getKey(), formatToolMetricName(entry.getKey()), entry.getValue()))
                .toList();
    }

    private List<AdminAiMonitorTrendVO> buildDailyTrends(List<AssistantChatLog> chatLogs, Date recentSince) {
        LocalDate startDate = recentSince.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Map<LocalDate, List<AssistantChatLog>> chatMap = chatLogs.stream()
                .filter(item -> item.getCreateTime() != null && !item.getCreateTime().before(recentSince))
                .collect(Collectors.groupingBy(item -> item.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
        Map<LocalDate, Long> fallbackMap = loadEventsByType(recentSince, "FALLBACK").stream()
                .filter(item -> item.getCreateTime() != null)
                .collect(Collectors.groupingBy(item -> item.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), Collectors.counting()));
        return java.util.stream.IntStream.range(0, Math.max(1, adminAiMonitorProperties.getRecentDays()))
                .mapToObj(offset -> startDate.plusDays(offset))
                .map(date -> buildDailyTrend(date, chatMap.getOrDefault(date, List.of()), fallbackMap.getOrDefault(date, 0L)))
                .toList();
    }

    private AdminAiMonitorTrendVO buildDailyTrend(LocalDate date, List<AssistantChatLog> dayLogs, Long fallbackCount) {
        AdminAiMonitorTrendVO trendVO = new AdminAiMonitorTrendVO();
        trendVO.setDate(date.toString());
        trendVO.setChatCount((long) dayLogs.size());
        trendVO.setAiAgentChatCount(dayLogs.stream()
                .filter(item -> Objects.equals(item.getIntentType(), AI_AGENT_INTENT))
                .count());
        trendVO.setFallbackCount(fallbackCount);
        return trendVO;
    }

    private List<AiMonitorEvent> loadEventsByType(Date recentSince, String eventType) {
        return aiMonitorEventService.list(new LambdaQueryWrapper<AiMonitorEvent>()
                .eq(StringTools.isNotEmpty(eventType), AiMonitorEvent::getEventType, eventType)
                .ge(AiMonitorEvent::getCreateTime, recentSince)
                .orderByDesc(AiMonitorEvent::getCreateTime));
    }

    private AdminAiMonitorRecentEventVO buildRecentEventVO(AiMonitorEvent event) {
        AdminAiMonitorRecentEventVO vo = new AdminAiMonitorRecentEventVO();
        vo.setEventSource(event.getEventSource());
        vo.setEventType(event.getEventType());
        vo.setEventCode(event.getEventCode());
        vo.setEventMessage(event.getEventMessage());
        vo.setUserId(event.getUserId());
        vo.setSessionId(event.getSessionId());
        vo.setCreateTime(event.getCreateTime());
        return vo;
    }

    private AdminAiMonitorMetricVO buildMetric(String metricKey, String metricName, Long count) {
        AdminAiMonitorMetricVO vo = new AdminAiMonitorMetricVO();
        vo.setMetricKey(metricKey);
        vo.setMetricName(metricName);
        vo.setCount(count);
        return vo;
    }

    private AdminAiServiceStatusVO buildStatus(String serviceKey, String serviceName, String status, String message) {
        AdminAiServiceStatusVO vo = new AdminAiServiceStatusVO();
        vo.setServiceKey(serviceKey);
        vo.setServiceName(serviceName);
        vo.setStatus(status);
        vo.setMessage(message);
        return vo;
    }

    private String formatMetricName(String eventCode) {
        return switch (eventCode) {
            case "assistant_disabled" -> "助手开关关闭";
            case "dependencies_unavailable" -> "依赖不可用降级";
            case "invoke_failed" -> "模型调用失败降级";
            case "http_status" -> "搜索接口异常降级";
            case "empty_result" -> "搜索无结果降级";
            case "ids_not_found" -> "索引数据不一致降级";
            case "exception" -> "搜索异常降级";
            case "agent_success" -> "助手调用成功";
            case "semantic_search_success" -> "语义搜索成功";
            default -> eventCode == null ? "未知事件" : eventCode.toLowerCase(Locale.ROOT);
        };
    }

    private String formatToolMetricName(String metricKey) {
        if (StringTools.isEmpty(metricKey)) {
            return "未知组件";
        }
        String[] parts = metricKey.split(":", 2);
        String source = parts.length > 0 ? parts[0] : "unknown";
        String type = parts.length > 1 ? parts[1] : "UNKNOWN";
        return switch (source) {
            case "assistant_agent" -> "智能助手 - " + type;
            case "semantic_search" -> "语义搜索 - " + type;
            default -> source.toLowerCase(Locale.ROOT) + " - " + type;
        };
    }

    private boolean checkHttpReachable(String url) {
        if (StringTools.isEmpty(url)) {
            return false;
        }
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.code() < 500;
        } catch (IOException e) {
            return false;
        }
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
