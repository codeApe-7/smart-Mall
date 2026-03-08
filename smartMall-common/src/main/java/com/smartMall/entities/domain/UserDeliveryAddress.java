package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户收货地址表。
 */
@Data
@TableName("user_delivery_address")
public class UserDeliveryAddress implements Serializable {

    @TableId("address_id")
    private String addressId;

    @TableField("user_id")
    private String userId;

    @TableField("receiver_name")
    private String receiverName;

    @TableField("receiver_phone")
    private String receiverPhone;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("region")
    private String region;

    @TableField("detail_address")
    private String detailAddress;

    @TableField("address_label")
    private String addressLabel;

    @TableField("default_address")
    private Integer defaultAddress;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
