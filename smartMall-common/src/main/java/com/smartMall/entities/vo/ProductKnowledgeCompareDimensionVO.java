package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Product knowledge compare dimension row.
 */
@Data
public class ProductKnowledgeCompareDimensionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dimensionKey;

    private String dimensionName;

    private List<ProductKnowledgeCompareCellVO> values;
}
