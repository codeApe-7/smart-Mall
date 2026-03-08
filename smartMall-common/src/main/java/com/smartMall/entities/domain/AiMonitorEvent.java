package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI monitor event entity.
 */
@Data
@TableName("ai_monitor_event")
public class AiMonitorEvent implements Serializable {

    @TableId("event_id")
    private String eventId;

    @TableField("event_source")
    private String eventSource;

    @TableField("event_type")
    private String eventType;

    @TableField("event_code")
    private String eventCode;

    @TableField("event_message")
    private String eventMessage;

    @TableField("user_id")
    private String userId;

    @TableField("session_id")
    private String sessionId;

    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
