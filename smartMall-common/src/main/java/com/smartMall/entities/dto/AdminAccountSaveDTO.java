package com.smartMall.entities.dto;

import lombok.Data;

import java.util.List;

/**
 * 后台账号保存参数。
 */
@Data
public class AdminAccountSaveDTO {

    private String accountId;

    private String accountName;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    private Integer status;

    private Boolean superAdmin;

    private String remark;

    private List<String> roleIds;
}
