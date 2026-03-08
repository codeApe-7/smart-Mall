package com.smartMall.entities.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Product knowledge query DTO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductKnowledgeQueryDTO extends PageQueryDTO {

    private String keyword;

    private String productId;

    private String categoryIdOrPCategoryId;

    private Boolean semanticSearch = true;
}
