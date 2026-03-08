package com.smartMall.entities.dto;

import lombok.Data;

/**
 * 后台账号状态变更参数。
 */
@Data
public class AdminAccountStatusDTO {

    private String accountId;

    private Integer status;
}
