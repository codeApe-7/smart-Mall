package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Product knowledge card for RAG retrieval.
 */
@Data
public class ProductKnowledgeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;

    private String productName;

    private String cover;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String sellingPointSummary;

    private String reviewSummary;

    private String afterSalesSummary;

    private String knowledgeText;

    private Integer reviewCount;

    private Double averageRating;

    private List<String> knowledgeTags;
}
