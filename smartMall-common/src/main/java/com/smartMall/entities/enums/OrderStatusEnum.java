package com.smartMall.entities.enums;

import java.util.Arrays;

/**
 * 订单状态枚举。
 */
public enum OrderStatusEnum {

    PENDING_PAYMENT(0, "待支付"),
    PAID(10, "已支付"),
    CANCELED(20, "已取消"),
    COMPLETED(30, "已完成");

    private final Integer status;
    private final String desc;

    OrderStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static OrderStatusEnum getByStatus(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .findFirst()
                .orElse(null);
    }
}
