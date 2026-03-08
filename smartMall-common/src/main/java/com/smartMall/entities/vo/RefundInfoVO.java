package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款信息视图。
 */
@Data
public class RefundInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String refundId;

    private String refundNo;

    private String orderId;

    private String orderNo;

    private BigDecimal refundAmount;

    private String refundReason;

    private Integer refundStatus;

    private String refundStatusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date approveTime;
}
