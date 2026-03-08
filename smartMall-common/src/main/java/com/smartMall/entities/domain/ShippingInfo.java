package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 物流记录表。
 */
@Data
@TableName("shipping_info")
public class ShippingInfo implements Serializable {

    @TableId("shipping_id")
    private String shippingId;

    @TableField("order_id")
    private String orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private String userId;

    @TableField("tracking_no")
    private String trackingNo;

    @TableField("shipping_company")
    private String shippingCompany;

    @TableField("shipping_status")
    private Integer shippingStatus;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("receive_time")
    private Date receiveTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
