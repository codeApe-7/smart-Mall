package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Product knowledge compare result.
 */
@Data
public class ProductKnowledgeCompareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean comparable;

    private List<ProductKnowledgeVO> products;

    private List<ProductKnowledgeCompareDimensionVO> dimensions;

    private String compareSummary;

    private List<String> decisionSuggestions;

    private String comparisonText;
}
