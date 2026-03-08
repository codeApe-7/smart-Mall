package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Pending shipment order card for admin dashboard.
 */
@Data
public class AdminPendingShipmentOrderVO implements Serializable {

    private String orderId;

    private String orderNo;

    private String userId;

    private String receiverName;

    private String receiverPhone;

    private BigDecimal totalAmount;

    private Integer totalQuantity;

    private Date payTime;
}
