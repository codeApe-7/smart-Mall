package com.smartMall.entities.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Product search properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-mall.search")
public class ProductSearchProperties {

    private boolean semanticEnabled;

    private String elasticsearchUri = "http://127.0.0.1:9200";

    private String productIndexName = "smart_mall_product";

    private Integer semanticCandidateSize = 50;
}
