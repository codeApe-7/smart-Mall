package com.smartMall.entities.dto;

import lombok.Data;

/**
 * 后台操作日志查询参数。
 */
@Data
public class AdminOperationLogQueryDTO extends PageQueryDTO {

    private String accountName;

    private String operationType;

    private Integer operationStatus;

    private String keyword;
}
