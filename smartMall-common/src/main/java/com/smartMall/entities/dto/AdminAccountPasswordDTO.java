package com.smartMall.entities.dto;

import lombok.Data;

/**
 * 后台账号重置密码参数。
 */
@Data
public class AdminAccountPasswordDTO {

    private String accountId;

    private String password;
}
