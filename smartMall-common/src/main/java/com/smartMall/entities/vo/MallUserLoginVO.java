package com.smartMall.entities.vo;

import lombok.Data;

/**
 * 用户登录返回结果。
 */
@Data
public class MallUserLoginVO {

    private String userToken;

    private MallUserProfileVO profile;
}
