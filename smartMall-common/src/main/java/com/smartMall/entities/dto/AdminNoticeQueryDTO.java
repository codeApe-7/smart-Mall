package com.smartMall.entities.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台消息通知查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminNoticeQueryDTO extends PageQueryDTO {

    private String keyword;

    private String messageType;

    private Integer publishStatus;

    private Integer targetType;
}
