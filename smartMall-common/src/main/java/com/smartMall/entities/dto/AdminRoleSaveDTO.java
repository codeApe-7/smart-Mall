package com.smartMall.entities.dto;

import lombok.Data;

import java.util.List;

/**
 * 后台角色保存参数。
 */
@Data
public class AdminRoleSaveDTO {

    private String roleId;

    private String roleCode;

    private String roleName;

    private Integer status;

    private String remark;

    private List<String> permissionCodes;
}
