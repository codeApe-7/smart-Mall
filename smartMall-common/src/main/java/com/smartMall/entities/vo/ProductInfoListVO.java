package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品列表展示VO
 *
 * @author 15712
 * @date 2026/2/13
 */
@Data
public class ProductInfoListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品描述
     */
    private String productDesc;

    /**
     * 封面
     */
    private String cover;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类父ID
     */
    @JsonProperty("pCategoryId")
    private String pCategoryId;

    /**
     * -1:已删除 0:下架 1:上架
     */
    private Integer status;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 销量
     */
    private Integer totalSale;

    /**
     * 0:未推荐 1:已推荐
     */
    private Integer commendType;

    // === 额外展示字段 ===

    /**
     * 分类名称（关联 sys_category）
     */
    private String categoryName;

    /**
     * SKU 数量
     */
    private Integer skuCount;

    /**
     * 总库存
     */
    private Integer totalStock;
}
