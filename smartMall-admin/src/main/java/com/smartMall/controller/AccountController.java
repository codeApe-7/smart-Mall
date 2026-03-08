package com.smartMall.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.smartMall.annotation.AdminAuditLog;
import com.smartMall.component.RedisComponent;
import com.smartMall.entities.dto.LoginDTO;
import com.smartMall.entities.enums.AdminOperationTypeEnum;
import com.smartMall.entities.vo.AdminCurrentAccountVO;
import com.smartMall.entities.vo.CheckCodeVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.AdminAuthorityManageService;
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

    @GetMapping("/checkCode")
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
    @AdminAuditLog(value = "后台账号登录", type = AdminOperationTypeEnum.LOGIN)
    public ResponseVO<String> login(@RequestBody @Valid LoginDTO loginDTO) {
        try {
            String cachedCode = redisComponent.getCheckCode(loginDTO.getCheckCodeKey());
            if (cachedCode == null || !loginDTO.getCheckCode().equalsIgnoreCase(cachedCode)) {
                throw new BusinessException("验证码错误");
            }
            String principal = adminAuthorityManageService.authenticate(loginDTO.getAccount(), loginDTO.getPassword());
            StpUtil.login(principal);
            return ResponseVO.success(StpUtil.getTokenValue());
        } finally {
            redisComponent.cleanCheckCode(loginDTO.getCheckCodeKey());
        }
    }

    @GetMapping("/profile")
    public ResponseVO<AdminCurrentAccountVO> profile() {
        StpUtil.checkLogin();
        return ResponseVO.success(adminAuthorityManageService.getCurrentAccount(StpUtil.getLoginIdAsString()));
    }

    @PostMapping("/logout")
    @AdminAuditLog(value = "后台账号退出登录", type = AdminOperationTypeEnum.LOGIN)
    public ResponseVO<Void> logout() {
        StpUtil.checkLogin();
        StpUtil.logout();
        return ResponseVO.success();
    }
}
