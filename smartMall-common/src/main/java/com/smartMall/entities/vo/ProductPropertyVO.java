package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品属性VO
 *
 * @author 
 * @date 2026/2/14
 */
@Data
public class ProductPropertyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 属性ID
     */
    private String propertyId;

    /**
     * 属性名称
     */
    private String propertyName;

    /**
     * 属性排序
     */
    private Integer propertySort;

    /**
     * 0:无需封面 1:需封面
     */
    private Integer coverType;

    private List<ProductPropertyValueVO> propertyValueList;
}