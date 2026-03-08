package com.smartMall.entities.vo;

import lombok.Data;

import java.util.Date;

/**
 * 后台消息通知详情视图。
 */
@Data
public class AdminNoticeDetailVO {

    private String noticeId;

    private String noticeTitle;

    private String noticeSummary;

    private String noticeContent;

    private String messageType;

    private String messageTypeDesc;

    private Integer targetType;

    private String targetTypeDesc;

    private String targetUserId;

    private Integer publishStatus;

    private String publishStatusDesc;

    private Integer readUserCount;

    private Date publishTime;

    private Date createTime;

    private Date updateTime;
}
