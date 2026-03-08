package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台消息通知保存参数。
 */
@Data
public class AdminNoticeSaveDTO {

    private String noticeId;

    @NotBlank(message = "noticeTitle can not be blank")
    private String noticeTitle;

    private String noticeSummary;

    @NotBlank(message = "noticeContent can not be blank")
    private String noticeContent;

    @NotBlank(message = "messageType can not be blank")
    private String messageType;

    private Integer targetType;

    private String targetUserId;
}
