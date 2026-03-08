package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Admin order list VO.
 */
@Data
public class AdminOrderListVO implements Serializable {

    private String orderId;

    private String orderNo;

    private String userId;

    private Integer orderStatus;

    private String orderStatusDesc;

    private BigDecimal totalAmount;

    private Integer totalQuantity;

    private String receiverName;

    private String receiverPhone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date payTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shipTime;
}
