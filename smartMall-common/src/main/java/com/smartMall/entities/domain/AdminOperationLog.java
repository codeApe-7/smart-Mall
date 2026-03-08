package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 后台操作审计日志实体。
 */
@Data
@TableName("admin_operation_log")
public class AdminOperationLog implements Serializable {

    @TableId("log_id")
    private String logId;

    @TableField("account_id")
    private String accountId;

    @TableField("account_name")
    private String accountName;

    @TableField("operation_type")
    private String operationType;

    @TableField("operation_name")
    private String operationName;

    @TableField("request_uri")
    private String requestUri;

    @TableField("request_method")
    private String requestMethod;

    @TableField("request_param")
    private String requestParam;

    @TableField("operation_status")
    private Integer operationStatus;

    @TableField("error_message")
    private String errorMessage;

    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
