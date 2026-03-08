package com.smartMall.entities.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Assistant chat response.
 */
@Data
public class AssistantChatResponseVO {

    private String sessionId;

    private String intentType;

    private String intentDesc;

    private String reply;

    private List<String> suggestions;

    private AssistantChatPayloadVO payload;

    private Date responseTime;
}
