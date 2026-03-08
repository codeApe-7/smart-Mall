package com.smartMall.entities.vo;

import lombok.Data;

import java.util.Date;

/**
 * Assistant chat history item.
 */
@Data
public class AssistantChatHistoryVO {

    private String chatId;

    private String sessionId;

    private String userId;

    private String requestText;

    private String intentType;

    private String intentDesc;

    private String replyText;

    private Date createTime;
}
