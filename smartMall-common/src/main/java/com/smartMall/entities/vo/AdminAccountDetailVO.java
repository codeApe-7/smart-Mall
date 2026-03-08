package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 后台账号详情。
 */
@Data
public class AdminAccountDetailVO implements Serializable {

    private String accountId;

    private String accountName;

    private String nickname;

    private String phone;

    private String email;

    private Integer status;

    private String statusDesc;

    private Boolean superAdmin;

    private String remark;

    private List<String> roleIds;

    private List<String> roleNames;

    private List<String> permissionCodes;

    private List<String> permissionNames;

    private Date lastLoginTime;

    private Date createTime;

    private Date updateTime;
}
