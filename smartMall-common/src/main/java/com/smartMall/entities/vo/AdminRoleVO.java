package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 后台角色信息。
 */
@Data
public class AdminRoleVO implements Serializable {

    private String roleId;

    private String roleCode;

    private String roleName;

    private Integer status;

    private String statusDesc;

    private String remark;

    private List<String> permissionCodes;

    private List<String> permissionNames;

    private Long accountCount;

    private Date createTime;

    private Date updateTime;
}
