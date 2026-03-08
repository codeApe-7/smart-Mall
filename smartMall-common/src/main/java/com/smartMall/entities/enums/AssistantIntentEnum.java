package com.smartMall.entities.enums;

import java.util.Arrays;

/**
 * Intelligent shopping assistant intent type.
 */
public enum AssistantIntentEnum {

    PRODUCT_SEARCH("PRODUCT_SEARCH", "search product"),
    PRODUCT_RECOMMEND("PRODUCT_RECOMMEND", "recommend product"),
    PRODUCT_DETAIL("PRODUCT_DETAIL", "product detail"),
    ORDER_LIST("ORDER_LIST", "order list"),
    ORDER_DETAIL("ORDER_DETAIL", "order detail"),
    ORDER_CANCEL("ORDER_CANCEL", "cancel order"),
    REFUND_APPLY("REFUND_APPLY", "apply refund"),
    REFUND_DETAIL("REFUND_DETAIL", "refund detail"),
    RECEIVE_CONFIRM("RECEIVE_CONFIRM", "confirm receive"),
    ORDER_REVIEW_QUERY("ORDER_REVIEW_QUERY", "query order reviews"),
    REVIEW_SUBMIT("REVIEW_SUBMIT", "submit review"),
    UNKNOWN("UNKNOWN", "unknown");

    private final String code;
    private final String desc;

    AssistantIntentEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AssistantIntentEnum getByCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
