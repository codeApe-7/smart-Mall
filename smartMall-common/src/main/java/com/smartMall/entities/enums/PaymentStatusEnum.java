package com.smartMall.entities.enums;

import java.util.Arrays;

/**
 * Payment status enum.
 */
public enum PaymentStatusEnum {

    PENDING(0, "待支付"),
    SUCCESS(10, "支付成功"),
    FAILED(20, "支付失败"),
    CLOSED(30, "已关闭");

    private final Integer status;
    private final String desc;

    PaymentStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static PaymentStatusEnum getByStatus(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .findFirst()
                .orElse(null);
    }
}
