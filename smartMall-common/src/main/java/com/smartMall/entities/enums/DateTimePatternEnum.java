package com.smartMall.entities.enums;

/**
 * 日期时间格式模式枚举
 *
 * @author 小新
 * @version 1.0
 * @date 2026/1/24
 */
public enum DateTimePatternEnum {

    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),
    YYYY_MM_DD("yyyy-MM-dd"),
    YYYYMM("yyyyMM");

    private final String pattern;

    DateTimePatternEnum(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
