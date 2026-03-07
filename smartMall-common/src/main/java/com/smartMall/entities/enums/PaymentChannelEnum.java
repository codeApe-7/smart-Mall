package com.smartMall.entities.enums;

import java.util.Arrays;

/**
 * Payment channel enum.
 */
public enum PaymentChannelEnum {

    ALIPAY_SANDBOX("ALIPAY_SANDBOX", "支付宝沙箱");

    private final String code;
    private final String desc;

    PaymentChannelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PaymentChannelEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
