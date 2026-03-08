package com.smartMall.entities.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Admin refund query DTO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminRefundQueryDTO extends PageQueryDTO {

    private String refundNo;

    private String orderNo;

    private String userId;

    private Integer refundStatus;
}
