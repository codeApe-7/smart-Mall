package com.smartMall.entities.dto;

import lombok.Data;

/**
 * 用户资料更新参数。
 */
@Data
public class MallUserProfileUpdateDTO {

    private String nickname;

    private String avatar;

    private String phone;
}
