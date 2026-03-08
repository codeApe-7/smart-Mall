package com.smartMall.entities.enums;

/**
 * 后台账号状态枚举。
 */
public enum AdminAccountStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer status;
    private final String desc;

    AdminAccountStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static AdminAccountStatusEnum getByStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (AdminAccountStatusEnum item : values()) {
            if (item.status.equals(status)) {
                return item;
            }
        }
        return null;
    }
}
