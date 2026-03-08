package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Admin knowledge index summary VO.
 */
@Data
public class AdminKnowledgeIndexSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean semanticSearchEnabled;

    private Boolean productKnowledgeEnabled;

    private String elasticsearchUri;

    private String productIndexName;

    private Boolean reachable;

    private Long onSaleProductCount;

    private Long indexedDocumentCount;

    private Date checkedTime;
}
