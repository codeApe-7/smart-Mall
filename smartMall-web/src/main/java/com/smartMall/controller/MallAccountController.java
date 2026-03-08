package com.smartMall.controller;

import com.smartMall.entities.dto.MallUserLoginDTO;
import com.smartMall.entities.dto.MallUserPasswordUpdateDTO;
import com.smartMall.entities.dto.MallUserProfileUpdateDTO;
import com.smartMall.entities.dto.MallUserRegisterDTO;
import com.smartMall.entities.vo.MallUserLoginVO;
import com.smartMall.entities.vo.MallUserProfileVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.MallUserCenterService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端账号控制器。
 */
@Slf4j
@RestController
@RequestMapping("/account")
public class MallAccountController {

    @Resource
    private MallUserCenterService mallUserCenterService;

    @PostMapping("/register")
    public ResponseVO<MallUserLoginVO> register(@RequestBody @Valid MallUserRegisterDTO dto) {
        log.info("web user register, username={}", dto.getUsername());
        return ResponseVO.success(mallUserCenterService.register(dto));
    }

    @PostMapping("/login")
    public ResponseVO<MallUserLoginVO> login(@RequestBody @Valid MallUserLoginDTO dto) {
        log.info("web user login, account={}", dto.getAccount());
        return ResponseVO.success(mallUserCenterService.login(dto));
    }

    @GetMapping("/profile")
    public ResponseVO<MallUserProfileVO> profile(@RequestParam String userToken) {
        return ResponseVO.success(mallUserCenterService.getCurrentProfile(userToken));
    }

    @PostMapping("/profile/save")
    public ResponseVO<Void> saveProfile(@RequestParam String userToken,
                                        @RequestBody MallUserProfileUpdateDTO dto) {
        mallUserCenterService.updateProfile(userToken, dto);
        return ResponseVO.success();
    }

    @PostMapping("/password/update")
    public ResponseVO<Void> updatePassword(@RequestParam String userToken,
                                           @RequestBody @Valid MallUserPasswordUpdateDTO dto) {
        mallUserCenterService.updatePassword(userToken, dto);
        return ResponseVO.success();
    }

    @PostMapping("/logout")
    public ResponseVO<Void> logout(@RequestParam String userToken) {
        mallUserCenterService.logout(userToken);
        return ResponseVO.success();
    }
}
