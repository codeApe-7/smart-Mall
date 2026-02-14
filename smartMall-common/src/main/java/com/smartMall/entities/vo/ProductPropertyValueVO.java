package com.smartMall.entities.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品属性值VO
 *
 * @author 15712
 * @date 2026/2/13
 */
@Data
public class ProductPropertyValueVO implements Serializable {

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
     * 属性封面
     */
    private String propertyCover;

    /**
     * 0:无需封面 1:需封面
     */
    private Integer coverType;

    /**
     * 属性值ID
     */
    @NotBlank(message = "属性值ID不能为空")
    private String propertyValueId;

    /**
     * 属性值
     */
    @NotBlank(message = "属性值不能为空")
    private String propertyValue;

    /**
     * 备注
     */
    private String propertyRemark;
}
