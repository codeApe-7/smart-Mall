package com.smartMall.service;

import com.smartMall.entities.dto.AdminNoticeQueryDTO;
import com.smartMall.entities.dto.AdminNoticeSaveDTO;
import com.smartMall.entities.vo.AdminNoticeDetailVO;
import com.smartMall.entities.vo.AdminNoticeListVO;
import com.smartMall.entities.vo.PageResultVO;

/**
 * 后台消息通知管理 Service。
 */
public interface AdminNoticeManageService {

    PageResultVO<AdminNoticeListVO> loadNoticeList(AdminNoticeQueryDTO dto);

    AdminNoticeDetailVO getNoticeDetail(String noticeId);

    void saveNotice(AdminNoticeSaveDTO dto);

    void publishNotice(String noticeId);

    void offlineNotice(String noticeId);

    void deleteNotice(String noticeId);
}
