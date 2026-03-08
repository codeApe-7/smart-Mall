package com.smartMall.controller;

import com.smartMall.annotation.AdminAuditLog;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.smartMall.entities.dto.AdminNoticeQueryDTO;
import com.smartMall.entities.dto.AdminNoticeSaveDTO;
import com.smartMall.entities.enums.AdminOperationTypeEnum;
import com.smartMall.entities.vo.AdminNoticeDetailVO;
import com.smartMall.entities.vo.AdminNoticeListVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.AdminNoticeManageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台消息通知管理控制器。
 */
@RestController
@RequestMapping("/notice")
@SaCheckPermission("notice:manage")
public class NoticeManageController {

    @Resource
    private AdminNoticeManageService adminNoticeManageService;

    @PostMapping("/list")
    public ResponseVO<PageResultVO<AdminNoticeListVO>> list(@RequestBody(required = false) AdminNoticeQueryDTO dto) {
        return ResponseVO.success(adminNoticeManageService.loadNoticeList(dto));
    }

    @GetMapping("/detail/{noticeId}")
    public ResponseVO<AdminNoticeDetailVO> detail(@PathVariable String noticeId) {
        return ResponseVO.success(adminNoticeManageService.getNoticeDetail(noticeId));
    }

    @PostMapping("/save")
    @AdminAuditLog(value = "保存消息通知", type = AdminOperationTypeEnum.NOTICE)
    public ResponseVO<Void> save(@RequestBody @Valid AdminNoticeSaveDTO dto) {
        adminNoticeManageService.saveNotice(dto);
        return ResponseVO.success();
    }

    @PostMapping("/publish/{noticeId}")
    @AdminAuditLog(value = "发布消息通知", type = AdminOperationTypeEnum.NOTICE)
    public ResponseVO<Void> publish(@PathVariable String noticeId) {
        adminNoticeManageService.publishNotice(noticeId);
        return ResponseVO.success();
    }

    @PostMapping("/offline/{noticeId}")
    @AdminAuditLog(value = "下线消息通知", type = AdminOperationTypeEnum.NOTICE)
    public ResponseVO<Void> offline(@PathVariable String noticeId) {
        adminNoticeManageService.offlineNotice(noticeId);
        return ResponseVO.success();
    }

    @PostMapping("/delete/{noticeId}")
    @AdminAuditLog(value = "删除消息通知", type = AdminOperationTypeEnum.NOTICE)
    public ResponseVO<Void> delete(@PathVariable String noticeId) {
        adminNoticeManageService.deleteNotice(noticeId);
        return ResponseVO.success();
    }
}


