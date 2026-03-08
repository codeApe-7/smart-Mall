package com.smartMall.entities.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Product knowledge properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-mall.rag.product")
public class ProductKnowledgeProperties {

    private boolean enabled;

    private Integer defaultPageSize = 3;

    private Integer maxReviewSnippetCount = 3;

    private Integer maxPropertySnippetCount = 4;

    private List<String> afterSalesHighlights = List.of(
            "支持7天无理由退货，商品保持完好可申请售后。",
            "如遇质量问题可联系平台客服协助处理退款或换货。",
            "默认工作时间内提供在线售后咨询与订单跟踪服务。");
}
