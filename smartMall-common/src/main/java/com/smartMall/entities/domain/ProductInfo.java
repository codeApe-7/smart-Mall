package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 商品信息
 * @TableName product_info
 */
@TableName(value ="product_info")
@Data
public class ProductInfo implements Serializable {
    /**
     * 商品ID
     */
    @TableId(value = "product_id")
    private String productId;

    /**
     * 商品名称
     */
    @TableField(value = "product_name")
    private String productName;

    /**
     * 商品描述
     */
    @TableField(value = "product_desc")
    private String productDesc;

    /**
     * 封面
     */
    @TableField(value = "cover")
    private String cover;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private String categoryId;

    /**
     * 分类父ID
     */
    @TableField(value = "p_category_id")
    private String pCategoryId;

    /**
     * -1:已删除 0:下架 1:上架
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 最低价格
     */
    @TableField(value = "min_price")
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    @TableField(value = "max_price")
    private BigDecimal maxPrice;

    /**
     * 销量
     */
    @TableField(value = "total_sale")
    private Integer totalSale;

    /**
     * 0:未推荐 1:已经推荐
     */
    @TableField(value = "commend_type")
    private Integer commendType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}