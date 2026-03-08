package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 后台权限项。
 */
@Data
public class AdminPermissionVO implements Serializable {

    private String code;

    private String name;

    private String description;
}
