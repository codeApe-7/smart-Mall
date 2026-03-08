package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Admin AI monitor overview VO.
 */
@Data
public class AdminAiMonitorOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer recentDays;

    private Long totalChatCount;

    private Long aiAgentChatCount;

    private Long ruleChatCount;

    private Long todayChatCount;

    private Long todayActiveUserCount;

    private Date lastChatTime;

    private AdminKnowledgeIndexSummaryVO knowledgeIndexSummary;

    private List<AdminAiServiceStatusVO> serviceStatuses;

    private List<AdminAiMonitorMetricVO> fallbackStats;

    private List<AdminAiMonitorMetricVO> errorCodeStats;

    private List<AdminAiMonitorMetricVO> toolInvokeStats;

    private List<AdminAiMonitorTrendVO> dailyTrends;

    private List<AdminAiMonitorRecentEventVO> recentEvents;
}
