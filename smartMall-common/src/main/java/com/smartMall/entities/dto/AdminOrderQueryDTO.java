package com.smartMall.entities.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Admin order query DTO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminOrderQueryDTO extends PageQueryDTO {

    private String orderNo;

    private String userId;

    private Integer orderStatus;
}
