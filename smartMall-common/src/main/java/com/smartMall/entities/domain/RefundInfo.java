package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款记录表。
 */
@Data
@TableName("refund_info")
public class RefundInfo implements Serializable {

    @TableId("refund_id")
    private String refundId;

    @TableField("refund_no")
    private String refundNo;

    @TableField("order_id")
    private String orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private String userId;

    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @TableField("refund_reason")
    private String refundReason;

    @TableField("refund_status")
    private Integer refundStatus;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("approve_time")
    private Date approveTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
