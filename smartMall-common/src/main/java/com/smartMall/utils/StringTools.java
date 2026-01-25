package com.smartMall.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

/**
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/25 13:55
 */
public class StringTools {

    public static String upperCaseFirstLetter(String field) {
        if (field == null || field.isEmpty()) {
            return field;
        }
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return Character.toUpperCase(field.charAt(0)) + field.substring(1);
    }

    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String encodeByMd5(String originStr) {
        return StringTools.isEmpty(originStr) ? null : DigestUtils.md5DigestAsHex(originStr.getBytes());
    }
}
