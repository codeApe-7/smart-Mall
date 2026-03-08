package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单主表。
 */
@Data
@TableName("order_info")
public class OrderInfo implements Serializable {

    @TableId("order_id")
    private String orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private String userId;

    @TableField("order_status")
    private Integer orderStatus;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("total_quantity")
    private Integer totalQuantity;

    @TableField("receiver_name")
    private String receiverName;

    @TableField("receiver_phone")
    private String receiverPhone;

    @TableField("receiver_address")
    private String receiverAddress;

    @TableField("order_remark")
    private String orderRemark;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("pay_time")
    private Date payTime;

    @TableField("cancel_time")
    private Date cancelTime;

    @TableField("refund_time")
    private Date refundTime;

    @TableField("ship_time")
    private Date shipTime;

    @TableField("receive_time")
    private Date receiveTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
