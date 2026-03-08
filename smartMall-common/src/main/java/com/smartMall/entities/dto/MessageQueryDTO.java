package com.smartMall.entities.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户消息查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageQueryDTO extends PageQueryDTO {

    private String messageType;

    private Boolean read;
}
