package com.smartMall.entities.enums;

import java.util.Arrays;

/**
 * 物流状态枚举。
 */
public enum ShippingStatusEnum {

    SHIPPED(0, "已发货"),
    IN_TRANSIT(10, "运输中"),
    DELIVERED(20, "已签收");

    private final Integer status;
    private final String desc;

    ShippingStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static ShippingStatusEnum getByStatus(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .findFirst()
                .orElse(null);
    }
}
