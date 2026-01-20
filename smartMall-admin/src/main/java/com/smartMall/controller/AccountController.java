package com.smartMall.controller;

import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2026/1/20 21:59
 */
@RequestMapping("/account")
public class AccountController {

    @RequestMapping("/checkCode")
    public String checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100,42);
        String checkCodeBse64 = captcha.toBase64();
        return checkCodeBse64;
    }
}
