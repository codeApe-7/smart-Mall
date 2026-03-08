package com.smartMall.entities.dto;

import lombok.Data;

import java.util.List;

/**
 * Product knowledge compare request DTO.
 */
@Data
public class ProductKnowledgeCompareDTO {

    private List<String> productIds;

    private String keyword;

    private Integer maxCount = 3;

    private Boolean semanticSearch = true;
}
