package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 物流信息视图。
 */
@Data
public class ShippingInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shippingId;

    private String orderId;

    private String orderNo;

    private String trackingNo;

    private String shippingCompany;

    private Integer shippingStatus;

    private String shippingStatusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date receiveTime;
}
