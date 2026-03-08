package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 后台权限分组。
 */
@Data
public class AdminPermissionGroupVO implements Serializable {

    private String groupCode;

    private String groupName;

    private List<AdminPermissionVO> permissions;
}
