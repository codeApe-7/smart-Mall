package com.smartMall.entities.enums;

/**
 * 后台操作类型枚举。
 */
public enum AdminOperationTypeEnum {

    LOGIN("login", "登录登出"),
    ACCOUNT("account", "账号管理"),
    ROLE("role", "角色权限"),
    PRODUCT("product", "商品分类"),
    ORDER("order", "订单退款"),
    REVIEW("review", "评价管理"),
    USER("user", "用户管理"),
    AI("ai", "AI 管理"),
    NOTICE("notice", "消息通知"),
    AUDIT("audit", "审计日志"),
    OTHER("other", "其他操作");

    private final String code;
    private final String desc;

    AdminOperationTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
