package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 监控按日趋势。
 */
@Data
public class AdminAiMonitorTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String date;

    private Long chatCount;

    private Long aiAgentChatCount;

    private Long fallbackCount;
}
