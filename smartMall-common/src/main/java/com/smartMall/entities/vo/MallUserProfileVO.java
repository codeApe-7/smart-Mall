package com.smartMall.entities.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户资料视图。
 */
@Data
public class MallUserProfileVO {

    private String userId;

    private String username;

    private String nickname;

    private String avatar;

    private String phone;

    private Integer status;

    private String statusDesc;

    private Date createTime;

    private Date lastActiveTime;
}
