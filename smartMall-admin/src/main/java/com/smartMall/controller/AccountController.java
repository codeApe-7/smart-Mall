package com.smartMall.controller;

import com.smartMall.component.RedisComponent;
import com.smartMall.entities.config.AppConfig;
import com.smartMall.entities.dto.LoginDTO;
import com.smartMall.entities.vo.CheckCodeVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/20 21:59
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @RequestMapping("/checkCode")
    public ResponseVO<CheckCodeVO> checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeBse64 = captcha.toBase64();
        String checkCodeKey = redisComponent.saveCheckCode(code);
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(checkCodeBse64);
        checkCodeVO.setCheckCodeKey(checkCodeKey);
        return ResponseVO.success(checkCodeVO);
    }

    @PostMapping("/login")
    public ResponseVO<String> login(@RequestBody @Valid LoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        String password = loginDTO.getPassword();
        try {
            if (!account.equalsIgnoreCase(appConfig.getAdminAccount())
                    || !password.equalsIgnoreCase(StringTools.encodeByMd5(appConfig.getAdminPassword()))) {
                throw new BusinessException("账号或密码错误");
            }
            String cachedCode = redisComponent.getCheckCode(loginDTO.getCheckCodeKey());
            if (cachedCode == null || !loginDTO.getCheckCode().equalsIgnoreCase(cachedCode)) {
                throw new BusinessException("验证码错误");
            }
            String token = redisComponent.saveToken(account);
            return ResponseVO.success(token);
        } finally {
            redisComponent.cleanCheckCode(loginDTO.getCheckCodeKey());
        }
    }

    @PostMapping("/logout")
    public ResponseVO<Void> logout(String adminToken) {
        redisComponent.cleanToken(adminToken);
        return ResponseVO.success();
    }
}
