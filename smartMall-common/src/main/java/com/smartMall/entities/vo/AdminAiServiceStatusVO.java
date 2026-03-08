package com.smartMall.entities.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Admin AI service status VO.
 */
@Data
public class AdminAiServiceStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String serviceKey;

    private String serviceName;

    private String status;

    private String message;
}
