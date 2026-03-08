package com.smartMall.entities.enums;

import com.smartMall.utils.StringTools;

/**
 * 消息通知类型枚举。
 */
public enum NoticeMessageTypeEnum {

    ANNOUNCEMENT("announcement", "公告"),
    SYSTEM("system", "系统消息"),
    PROMOTION("promotion", "活动通知");

    private final String code;
    private final String desc;

    NoticeMessageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static NoticeMessageTypeEnum getByCode(String code) {
        if (StringTools.isEmpty(code)) {
            return null;
        }
        for (NoticeMessageTypeEnum item : values()) {
            if (item.code.equalsIgnoreCase(code.trim())) {
                return item;
            }
        }
        return null;
    }
}
