package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Admin AI monitor metric VO.
 */
@Data
public class AdminAiMonitorMetricVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String metricKey;

    private String metricName;

    private Long count;
}
