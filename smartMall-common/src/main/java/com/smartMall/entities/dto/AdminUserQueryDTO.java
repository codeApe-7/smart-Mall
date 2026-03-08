package com.smartMall.entities.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Admin user query DTO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserQueryDTO extends PageQueryDTO {

    private String userId;

    private String keyword;

    private String phone;

    private Integer status;
}
