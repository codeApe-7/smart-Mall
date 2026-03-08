package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Admin AI recent event VO.
 */
@Data
public class AdminAiMonitorRecentEventVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventSource;

    private String eventType;

    private String eventCode;

    private String eventMessage;

    private String userId;

    private String sessionId;

    private Date createTime;
}
