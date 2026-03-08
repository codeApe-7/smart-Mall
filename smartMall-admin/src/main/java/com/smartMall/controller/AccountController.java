package com.smartMall.controller;

import com.smartMall.component.RedisComponent;
import com.smartMall.entities.dto.LoginDTO;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.AdminCurrentAccountVO;
import com.smartMall.entities.vo.CheckCodeVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.AdminAuthorityManageService;
import com.smartMall.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台账号登录控制器。
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AdminAuthorityManageService adminAuthorityManageService;

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
        try {
            String cachedCode = redisComponent.getCheckCode(loginDTO.getCheckCodeKey());
            if (cachedCode == null || !loginDTO.getCheckCode().equalsIgnoreCase(cachedCode)) {
                throw new BusinessException("验证码错误");
            }
            String principal = adminAuthorityManageService.authenticate(loginDTO.getAccount(), loginDTO.getPassword());
            String token = redisComponent.saveToken(principal);
            return ResponseVO.success(token);
        } finally {
            redisComponent.cleanCheckCode(loginDTO.getCheckCodeKey());
        }
    }

    @GetMapping("/profile")
    public ResponseVO<AdminCurrentAccountVO> profile(String adminToken) {
        String principal = redisComponent.getToken(adminToken);
        if (StringTools.isEmpty(principal)) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED, "admin token is invalid");
        }
        return ResponseVO.success(adminAuthorityManageService.getCurrentAccount(principal));
    }

    @PostMapping("/logout")
    public ResponseVO<Void> logout(String adminToken) {
        redisComponent.cleanToken(adminToken);
        return ResponseVO.success();
    }
}
