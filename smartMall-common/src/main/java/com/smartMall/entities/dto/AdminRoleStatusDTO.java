package com.smartMall.entities.dto;

import lombok.Data;

/**
 * 后台角色状态变更参数。
 */
@Data
public class AdminRoleStatusDTO {

    private String roleId;

    private Integer status;
}
