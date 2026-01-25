package com.smartMall.exception;

import com.smartMall.entities.enums.ResponseCodeEnum;

/**
 * 自定义业务异常类
 *
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/24 23:40
 */
public class BusinessException extends RuntimeException {

    private Integer code;
    private String message;

    public BusinessException(String message) {
        super(message);
        this.code = ResponseCodeEnum.ERROR.getCode();
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResponseCodeEnum codeEnum) {
        super(codeEnum.getInfo());
        this.code = codeEnum.getCode();
        this.message = codeEnum.getInfo();
    }

    public BusinessException(ResponseCodeEnum codeEnum, String message) {
        super(message);
        this.code = codeEnum.getCode();
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
