package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单列表视图。
 */
@Data
public class OrderInfoListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderId;

    private String orderNo;

    private Integer orderStatus;

    private String orderStatusDesc;

    private BigDecimal totalAmount;

    private Integer totalQuantity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;
}
