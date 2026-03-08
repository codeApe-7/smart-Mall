package com.smartMall.entities.enums;

import java.util.Arrays;

/**
 * 退款状态枚举。
 */
public enum RefundStatusEnum {

    PENDING(0, "退款申请中"),
    APPROVED(10, "退款成功"),
    REJECTED(20, "退款拒绝");

    private final Integer status;
    private final String desc;

    RefundStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static RefundStatusEnum getByStatus(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .findFirst()
                .orElse(null);
    }
}
