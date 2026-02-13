package com.smartMall.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
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

    /**
     * 生成指定长度的随机数字字符串
     *
     * @param length 长度
     * @return 随机数字字符串
     */
    public static String getRandomNumber(int length) {
        return RandomUtil.randomNumbers(length);
    }

    /**
     * 获取文件后缀名（不含点），若无后缀或为目录则返回空字符串
     *
     * @param fileName 文件名（可含路径）
     * @return 后缀名，如 "jpg"、"pdf"；无后缀时返回 ""
     */
    public static String getFileSuffix(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        // 使用 Hutool 工具类提取后缀（推荐，与 FileUtil 一致）
        String suffix = FileUtil.getSuffix(fileName);
        return "." + StringUtils.defaultIfBlank(suffix, "");
    }

    public static boolean pathIsOk(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        return !path.contains("../") && !path.contains("..\\");
    }
}
