package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Intelligent shopping assistant chat request.
 */
@Data
public class AssistantChatRequestDTO {

    @NotBlank(message = "userId can not be blank")
    private String userId;

    private String sessionId;

    @NotBlank(message = "message can not be blank")
    private String message;

    private String productId;

    private String orderId;
}
