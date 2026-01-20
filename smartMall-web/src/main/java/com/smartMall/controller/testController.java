package com.smartMall.controller;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2026/1/20 21:31
 */
@RequestMapping("/test")
public class testController {
    @RequestMapping("/test1")
    public String test(){
        return "test";
    }
}
