package com.smartMall.entities.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends PageQueryDTO {

    private String userId;

    private Integer orderStatus;
}
