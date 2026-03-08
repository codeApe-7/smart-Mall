package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Admin user status DTO.
 */
@Data
public class AdminUserStatusDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    @NotNull(message = "status can not be null")
    private Integer status;
}
