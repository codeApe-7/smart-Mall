package com.smartMall.entities.enums;

/**
 * 响应状态码枚举
 *
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/24 23:40
 */
public enum ResponseCodeEnum {

    SUCCESS(200, "success", "请求成功"),
    ERROR(600, "error", "业务错误"),
    PARAM_ERROR(601, "error", "参数校验失败"),
    SYSTEM_ERROR(500, "error", "系统异常"),
    UNAUTHORIZED(401, "error", "未授权"),
    FORBIDDEN(403, "error", "禁止访问");

    private final Integer code;
    private final String status;
    private final String info;

    ResponseCodeEnum(Integer code, String status, String info) {
        this.code = code;
        this.status = status;
        this.info = info;
    }

    public Integer getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }
}
