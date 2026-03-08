package com.smartMall.service;

import com.smartMall.entities.dto.AdminUserQueryDTO;
import com.smartMall.entities.dto.AdminUserStatusDTO;
import com.smartMall.entities.vo.AdminUserDetailVO;
import com.smartMall.entities.vo.AdminUserListVO;
import com.smartMall.entities.vo.PageResultVO;

/**
 * Admin user manage service.
 */
public interface AdminUserManageService {

    PageResultVO<AdminUserListVO> loadUserList(AdminUserQueryDTO dto);

    AdminUserDetailVO getUserDetail(String userId);

    void updateUserStatus(AdminUserStatusDTO dto);
}
