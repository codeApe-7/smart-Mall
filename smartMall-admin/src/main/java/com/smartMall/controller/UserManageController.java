package com.smartMall.controller;

import com.smartMall.annotation.AdminAuditLog;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.smartMall.entities.dto.AdminUserQueryDTO;
import com.smartMall.entities.dto.AdminUserStatusDTO;
import com.smartMall.entities.vo.AdminUserDetailVO;
import com.smartMall.entities.vo.AdminUserListVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.entities.enums.AdminOperationTypeEnum;
import com.smartMall.service.AdminUserManageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin user manage controller.
 */
@RestController
@SaCheckPermission("user:manage")
@RequestMapping("/user")
public class UserManageController {

    @Resource
    private AdminUserManageService adminUserManageService;

    @PostMapping("/list")
    public ResponseVO<PageResultVO<AdminUserListVO>> list(@RequestBody(required = false) AdminUserQueryDTO dto) {
        return ResponseVO.success(adminUserManageService.loadUserList(dto));
    }

    @GetMapping("/detail/{userId}")
    public ResponseVO<AdminUserDetailVO> detail(@PathVariable String userId) {
        return ResponseVO.success(adminUserManageService.getUserDetail(userId));
    }

    @PostMapping("/status")
    @AdminAuditLog(value = "更新用户状态", type = AdminOperationTypeEnum.USER)
    public ResponseVO<Void> updateStatus(@RequestBody @Valid AdminUserStatusDTO dto) {
        adminUserManageService.updateUserStatus(dto);
        return ResponseVO.success();
    }
}


