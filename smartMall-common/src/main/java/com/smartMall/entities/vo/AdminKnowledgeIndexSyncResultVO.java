package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Admin knowledge index sync result VO.
 */
@Data
public class AdminKnowledgeIndexSyncResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String operationType;

    private String productIndexName;

    private Integer requestedCount;

    private Integer successCount;

    private Integer failCount;

    private List<String> failedProductIds;

    private Long durationMs;

    private Date completedTime;
}
