package com.smartMall.service;

import com.smartMall.entities.dto.AdminAccountPasswordDTO;
import com.smartMall.entities.dto.AdminAccountQueryDTO;
import com.smartMall.entities.dto.AdminAccountSaveDTO;
import com.smartMall.entities.dto.AdminAccountStatusDTO;
import com.smartMall.entities.dto.AdminRoleQueryDTO;
import com.smartMall.entities.dto.AdminRoleSaveDTO;
import com.smartMall.entities.dto.AdminRoleStatusDTO;
import com.smartMall.entities.vo.AdminAccountDetailVO;
import com.smartMall.entities.vo.AdminAccountListVO;
import com.smartMall.entities.vo.AdminCurrentAccountVO;
import com.smartMall.entities.vo.AdminPermissionGroupVO;
import com.smartMall.entities.vo.AdminRoleVO;
import com.smartMall.entities.vo.PageResultVO;

import java.util.List;

/**
 * 后台账户与权限管理 Service。
 */
public interface AdminAuthorityManageService {

    PageResultVO<AdminAccountListVO> loadAccountList(AdminAccountQueryDTO dto);

    AdminAccountDetailVO getAccountDetail(String accountId);

    void saveAccount(AdminAccountSaveDTO dto);

    void updateAccountStatus(AdminAccountStatusDTO dto);

    void resetPassword(AdminAccountPasswordDTO dto);

    PageResultVO<AdminRoleVO> loadRoleList(AdminRoleQueryDTO dto);

    void saveRole(AdminRoleSaveDTO dto);

    void updateRoleStatus(AdminRoleStatusDTO dto);

    List<AdminPermissionGroupVO> listPermissionGroups();

    String authenticate(String accountName, String password);

    AdminCurrentAccountVO getCurrentAccount(String principal);
}
