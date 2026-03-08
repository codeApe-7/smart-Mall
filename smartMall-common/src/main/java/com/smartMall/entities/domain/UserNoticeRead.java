package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户消息已读记录实体。
 */
@Data
@TableName("user_notice_read")
public class UserNoticeRead implements Serializable {

    @TableId("read_id")
    private String readId;

    @TableField("notice_id")
    private String noticeId;

    @TableField("user_id")
    private String userId;

    @TableField("read_time")
    private Date readTime;

    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
