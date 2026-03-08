package com.smartMall.entities.enums;

import com.smartMall.utils.StringTools;

/**
 * 后台权限枚举。
 */
public enum AdminPermissionEnum {

    DASHBOARD_OVERVIEW("dashboard:overview", "查看数据看板", "dashboard", "数据概览", "查看后台首页经营概览"),
    PRODUCT_MANAGE("product:manage", "商品分类与商品管理", "trade", "交易管理", "维护商品分类、商品信息与素材上传"),
    ORDER_MANAGE("order:manage", "订单与退款管理", "trade", "交易管理", "查询订单、发货与处理退款"),
    REVIEW_MANAGE("review:manage", "评价管理", "trade", "交易管理", "查询评价、回复评价与删除评价"),
    USER_MANAGE("user:manage", "用户管理", "user", "用户运营", "查看用户画像并维护启用禁用状态"),
    AI_CONFIG("ai:config", "AI 配置管理", "ai", "AI 管理", "维护智能助手、语义搜索与知识增强配置"),
    KNOWLEDGE_MANAGE("knowledge:manage", "知识库维护", "ai", "AI 管理", "预览商品知识卡片并同步或重建索引"),
    AI_MONITOR("ai:monitor", "AI 服务监控", "ai", "AI 管理", "查看 AI 服务状态、降级原因与最近事件"),
    NOTICE_MANAGE("notice:manage", "消息通知管理", "system", "系统管理", "维护后台公告与系统消息"),
    AUTHORITY_MANAGE("authority:manage", "账户权限管理", "system", "系统管理", "维护后台账号、角色与权限"),
    AUDIT_LOG("audit:log", "操作审计日志", "system", "系统管理", "查看后台操作审计日志");

    private final String code;
    private final String name;
    private final String groupCode;
    private final String groupName;
    private final String description;

    AdminPermissionEnum(String code, String name, String groupCode, String groupName, String description) {
        this.code = code;
        this.name = name;
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public static AdminPermissionEnum getByCode(String code) {
        if (StringTools.isEmpty(code)) {
            return null;
        }
        for (AdminPermissionEnum item : values()) {
            if (item.code.equalsIgnoreCase(code.trim())) {
                return item;
            }
        }
        return null;
    }
}
