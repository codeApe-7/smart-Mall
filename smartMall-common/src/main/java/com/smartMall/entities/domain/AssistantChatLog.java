package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Intelligent shopping assistant chat log.
 */
@Data
@TableName("assistant_chat_log")
public class AssistantChatLog implements Serializable {

    @TableId("chat_id")
    private String chatId;

    @TableField("session_id")
    private String sessionId;

    @TableField("user_id")
    private String userId;

    @TableField("request_text")
    private String requestText;

    @TableField("intent_type")
    private String intentType;

    @TableField("reply_text")
    private String replyText;

    @TableField("payload_summary")
    private String payloadSummary;

    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
