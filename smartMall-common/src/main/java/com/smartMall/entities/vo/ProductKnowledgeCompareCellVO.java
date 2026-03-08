package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * One compare cell for a product under one dimension.
 */
@Data
public class ProductKnowledgeCompareCellVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productId;

    private String productName;

    private String value;

    private Boolean highlight;
}
