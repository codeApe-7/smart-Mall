package com.smartMall.entities.dto;

import lombok.Data;

/**
 * 后台角色分页查询参数。
 */
@Data
public class AdminRoleQueryDTO extends PageQueryDTO {

    private String keyword;

    private Integer status;
}
