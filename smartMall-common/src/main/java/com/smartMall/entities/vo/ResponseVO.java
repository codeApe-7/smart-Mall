package com.smartMall.entities.vo;

import com.smartMall.entities.enums.ResponseCodeEnum;
import lombok.Data;

/**
 * 统一响应包装类
 *
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/24 23:40
 */
@Data
public class ResponseVO<T> {

    private String status;
    private Integer code;
    private String info;
    private T data;

    public ResponseVO() {
    }

    public ResponseVO(String status, Integer code, String info, T data) {
        this.status = status;
        this.code = code;
        this.info = info;
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ResponseVO<T> success() {
        return new ResponseVO<>(
                ResponseCodeEnum.SUCCESS.getStatus(),
                ResponseCodeEnum.SUCCESS.getCode(),
                ResponseCodeEnum.SUCCESS.getInfo(),
                null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ResponseVO<T> success(T data) {
        return new ResponseVO<>(
                ResponseCodeEnum.SUCCESS.getStatus(),
                ResponseCodeEnum.SUCCESS.getCode(),
                ResponseCodeEnum.SUCCESS.getInfo(),
                data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> ResponseVO<T> success(String info, T data) {
        return new ResponseVO<>(
                ResponseCodeEnum.SUCCESS.getStatus(),
                ResponseCodeEnum.SUCCESS.getCode(),
                info,
                data);
    }

    /**
     * 错误响应（仅消息）
     */
    public static <T> ResponseVO<T> error(String info) {
        return new ResponseVO<>(
                ResponseCodeEnum.ERROR.getStatus(),
                ResponseCodeEnum.ERROR.getCode(),
                info,
                null);
    }

    /**
     * 错误响应（带状态码和消息）
     */
    public static <T> ResponseVO<T> error(Integer code, String info) {
        return new ResponseVO<>(
                ResponseCodeEnum.ERROR.getStatus(),
                code,
                info,
                null);
    }

    /**
     * 错误响应（使用枚举）
     */
    public static <T> ResponseVO<T> error(ResponseCodeEnum codeEnum) {
        return new ResponseVO<>(
                codeEnum.getStatus(),
                codeEnum.getCode(),
                codeEnum.getInfo(),
                null);
    }

    /**
     * 错误响应（使用枚举和自定义消息）
     */
    public static <T> ResponseVO<T> error(ResponseCodeEnum codeEnum, String info) {
        return new ResponseVO<>(
                codeEnum.getStatus(),
                codeEnum.getCode(),
                info,
                null);
    }
}
