package com.smartMall.entities.enums;

/**
 * 消息通知发布状态枚举。
 */
public enum NoticePublishStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下线");

    private final Integer status;
    private final String desc;

    NoticePublishStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static NoticePublishStatusEnum getByStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (NoticePublishStatusEnum item : values()) {
            if (item.status.equals(status)) {
                return item;
            }
        }
        return null;
    }
}
