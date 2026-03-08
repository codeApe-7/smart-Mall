package com.smartMall.config;

import cn.dev33.satoken.stp.StpInterface;
import com.smartMall.entities.vo.AdminCurrentAccountVO;
import com.smartMall.service.AdminAuthorityManageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限与角色加载实现。
 */
@Component
public class AdminStpInterfaceImpl implements StpInterface {

    @Resource
    private AdminAuthorityManageService adminAuthorityManageService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        AdminCurrentAccountVO currentAccount = adminAuthorityManageService.getCurrentAccount(String.valueOf(loginId));
        return currentAccount == null || currentAccount.getPermissionCodes() == null
                ? List.of()
                : currentAccount.getPermissionCodes();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        AdminCurrentAccountVO currentAccount = adminAuthorityManageService.getCurrentAccount(String.valueOf(loginId));
        return currentAccount == null || currentAccount.getRoleCodes() == null
                ? List.of()
                : currentAccount.getRoleCodes();
    }
}
