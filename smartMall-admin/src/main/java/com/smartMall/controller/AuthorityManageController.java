package com.smartMall.controller;

import com.smartMall.annotation.AdminAuditLog;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.smartMall.entities.dto.AdminAccountPasswordDTO;
import com.smartMall.entities.dto.AdminAccountQueryDTO;
import com.smartMall.entities.dto.AdminAccountSaveDTO;
import com.smartMall.entities.dto.AdminAccountStatusDTO;
import com.smartMall.entities.dto.AdminRoleQueryDTO;
import com.smartMall.entities.dto.AdminRoleSaveDTO;
import com.smartMall.entities.dto.AdminRoleStatusDTO;
import com.smartMall.entities.enums.AdminOperationTypeEnum;
import com.smartMall.entities.vo.AdminAccountDetailVO;
import com.smartMall.entities.vo.AdminAccountListVO;
import com.smartMall.entities.vo.AdminPermissionGroupVO;
import com.smartMall.entities.vo.AdminRoleVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.AdminAuthorityManageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台账户与权限管理控制器。
 */
@RestController
@SaCheckPermission("authority:manage")
@RequestMapping("/authority")
public class AuthorityManageController {

    @Resource
    private AdminAuthorityManageService adminAuthorityManageService;

    @PostMapping("/account/list")
    public ResponseVO<PageResultVO<AdminAccountListVO>> loadAccountList(@RequestBody(required = false) AdminAccountQueryDTO dto) {
        return ResponseVO.success(adminAuthorityManageService.loadAccountList(dto));
    }

    @GetMapping("/account/detail/{accountId}")
    public ResponseVO<AdminAccountDetailVO> accountDetail(@PathVariable String accountId) {
        return ResponseVO.success(adminAuthorityManageService.getAccountDetail(accountId));
    }

    @PostMapping("/account/save")
    @AdminAuditLog(value = "保存后台账号", type = AdminOperationTypeEnum.ACCOUNT)
    public ResponseVO<Void> saveAccount(@RequestBody AdminAccountSaveDTO dto) {
        adminAuthorityManageService.saveAccount(dto);
        return ResponseVO.success();
    }

    @PostMapping("/account/status")
    @AdminAuditLog(value = "更新后台账号状态", type = AdminOperationTypeEnum.ACCOUNT)
    public ResponseVO<Void> updateAccountStatus(@RequestBody AdminAccountStatusDTO dto) {
        adminAuthorityManageService.updateAccountStatus(dto);
        return ResponseVO.success();
    }

    @PostMapping("/account/reset-password")
    @AdminAuditLog(value = "重置后台账号密码", type = AdminOperationTypeEnum.ACCOUNT)
    public ResponseVO<Void> resetPassword(@RequestBody AdminAccountPasswordDTO dto) {
        adminAuthorityManageService.resetPassword(dto);
        return ResponseVO.success();
    }

    @PostMapping("/role/list")
    public ResponseVO<PageResultVO<AdminRoleVO>> loadRoleList(@RequestBody(required = false) AdminRoleQueryDTO dto) {
        return ResponseVO.success(adminAuthorityManageService.loadRoleList(dto));
    }

    @PostMapping("/role/save")
    @AdminAuditLog(value = "保存后台角色", type = AdminOperationTypeEnum.ROLE)
    public ResponseVO<Void> saveRole(@RequestBody AdminRoleSaveDTO dto) {
        adminAuthorityManageService.saveRole(dto);
        return ResponseVO.success();
    }

    @PostMapping("/role/status")
    @AdminAuditLog(value = "更新后台角色状态", type = AdminOperationTypeEnum.ROLE)
    public ResponseVO<Void> updateRoleStatus(@RequestBody AdminRoleStatusDTO dto) {
        adminAuthorityManageService.updateRoleStatus(dto);
        return ResponseVO.success();
    }

    @GetMapping("/permission/list")
    public ResponseVO<List<AdminPermissionGroupVO>> permissionList() {
        return ResponseVO.success(adminAuthorityManageService.listPermissionGroups());
    }
}


