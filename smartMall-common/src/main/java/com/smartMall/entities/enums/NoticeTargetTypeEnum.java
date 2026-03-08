package com.smartMall.entities.enums;

/**
 * 消息通知目标类型枚举。
 */
public enum NoticeTargetTypeEnum {

    ALL_USER(0, "全体用户"),
    SPECIFIED_USER(1, "指定用户");

    private final Integer type;
    private final String desc;

    NoticeTargetTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static NoticeTargetTypeEnum getByType(Integer type) {
        if (type == null) {
            return null;
        }
        for (NoticeTargetTypeEnum item : values()) {
            if (item.type.equals(type)) {
                return item;
            }
        }
        return null;
    }
}
