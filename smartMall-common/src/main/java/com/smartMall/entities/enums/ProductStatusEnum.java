package com.smartMall.entities.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * 商品状态枚举
 *
 * @author 小新
 * @version 1.0
 * @date 2026/1/24
 */
public enum ProductStatusEnum {

    DELETE(-1, "已删除"),
    OFF_SALE(0, "未上架"),
    ON_SALE(1, "已上架");

    private final Integer status;
    private final String desc;

    ProductStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据状态码获取枚举实例
     *
     * @param status 状态码
     * @return 对应枚举，若不存在则返回 null
     */
    public static ProductStatusEnum getByStatus(Integer status) {
        return Arrays.stream(ProductStatusEnum.values())
                .filter(value -> value.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }
}
