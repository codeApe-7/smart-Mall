package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统消息通知实体。
 */
@Data
@TableName("sys_notice_message")
public class SysNoticeMessage implements Serializable {

    @TableId("notice_id")
    private String noticeId;

    @TableField("notice_title")
    private String noticeTitle;

    @TableField("notice_summary")
    private String noticeSummary;

    @TableField("notice_content")
    private String noticeContent;

    @TableField("message_type")
    private String messageType;

    @TableField("target_type")
    private Integer targetType;

    @TableField("target_user_id")
    private String targetUserId;

    @TableField("publish_status")
    private Integer publishStatus;

    @TableField("publish_time")
    private Date publishTime;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
