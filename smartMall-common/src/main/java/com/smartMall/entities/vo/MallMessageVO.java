package com.smartMall.entities.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户消息中心视图。
 */
@Data
public class MallMessageVO {

    private String noticeId;

    private String noticeTitle;

    private String noticeSummary;

    private String noticeContent;

    private String messageType;

    private String messageTypeDesc;

    private Boolean read;

    private Date publishTime;
}
