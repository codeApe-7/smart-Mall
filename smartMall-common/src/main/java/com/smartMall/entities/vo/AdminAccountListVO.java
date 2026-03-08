package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 后台账号列表项。
 */
@Data
public class AdminAccountListVO implements Serializable {

    private String accountId;

    private String accountName;

    private String nickname;

    private Integer status;

    private String statusDesc;

    private Boolean superAdmin;

    private List<String> roleNames;

    private Date lastLoginTime;

    private Date createTime;
}
