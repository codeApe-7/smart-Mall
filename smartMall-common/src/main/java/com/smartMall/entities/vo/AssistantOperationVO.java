package com.smartMall.entities.vo;

import lombok.Data;

/**
 * Assistant operation result.
 */
@Data
public class AssistantOperationVO {

    private String action;

    private String orderId;

    private Integer orderStatus;

    private String orderStatusDesc;

    private String message;
}
