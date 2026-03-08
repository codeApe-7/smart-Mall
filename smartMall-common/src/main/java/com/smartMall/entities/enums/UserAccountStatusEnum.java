package com.smartMall.entities.enums;

import java.util.Arrays;

/**
 * 用户账户状态枚举。
 */
public enum UserAccountStatusEnum {

    DISABLED(0, "disabled"),
    ENABLED(1, "enabled");

    private final Integer status;
    private final String desc;

    UserAccountStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static UserAccountStatusEnum getByStatus(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .findFirst()
                .orElse(null);
    }
}
