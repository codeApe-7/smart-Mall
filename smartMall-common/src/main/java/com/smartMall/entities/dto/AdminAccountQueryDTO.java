package com.smartMall.entities.dto;

import lombok.Data;

/**
 * 后台账号分页查询参数。
 */
@Data
public class AdminAccountQueryDTO extends PageQueryDTO {

    private String keyword;

    private Integer status;

    private String roleId;
}
