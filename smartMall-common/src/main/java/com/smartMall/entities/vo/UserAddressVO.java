package com.smartMall.entities.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户收货地址视图。
 */
@Data
public class UserAddressVO {

    private String addressId;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String city;

    private String region;

    private String detailAddress;

    private String fullAddress;

    private String addressLabel;

    private Integer defaultAddress;

    private Date createTime;

    private Date updateTime;
}
