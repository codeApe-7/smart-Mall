package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Intelligent shopping assistant history query.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssistantHistoryQueryDTO extends PageQueryDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    private String sessionId;
}
