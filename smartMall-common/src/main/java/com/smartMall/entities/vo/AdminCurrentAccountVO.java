package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 当前登录后台账号信息。
 */
@Data
public class AdminCurrentAccountVO implements Serializable {

    private String accountId;

    private String accountName;

    private String nickname;

    private Boolean superAdmin;

    private List<String> roleCodes;

    private List<String> roleNames;

    private List<String> permissionCodes;

    private Date lastLoginTime;
}
