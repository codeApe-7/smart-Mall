package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 后台操作日志返回对象。
 */
@Data
public class AdminOperationLogVO implements Serializable {

    private String logId;

    private String accountId;

    private String accountName;

    private String operationType;

    private String operationName;

    private String requestUri;

    private String requestMethod;

    private Integer operationStatus;

    private String errorMessage;

    private String requestParam;

    private Date createTime;
}
