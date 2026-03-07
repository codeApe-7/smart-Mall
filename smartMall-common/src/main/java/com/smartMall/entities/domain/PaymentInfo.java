package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Payment record.
 */
@Data
@TableName("payment_info")
public class PaymentInfo implements Serializable {

    @TableId("payment_id")
    private String paymentId;

    @TableField("payment_no")
    private String paymentNo;

    @TableField("order_id")
    private String orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private String userId;

    @TableField("pay_channel")
    private String payChannel;

    @TableField("pay_status")
    private Integer payStatus;

    @TableField("pay_amount")
    private BigDecimal payAmount;

    @TableField("gateway_trade_no")
    private String gatewayTradeNo;

    @TableField("callback_content")
    private String callbackContent;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("pay_time")
    private Date payTime;

    @TableField("callback_time")
    private Date callbackTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
