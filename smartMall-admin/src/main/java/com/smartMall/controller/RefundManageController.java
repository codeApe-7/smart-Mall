package com.smartMall.controller;

import com.smartMall.entities.dto.AdminRefundQueryDTO;
import com.smartMall.entities.vo.AdminRefundInfoVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.AdminOrderManageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin refund manage controller.
 */
@RestController
@RequestMapping("/refund")
public class RefundManageController {

    @Resource
    private AdminOrderManageService adminOrderManageService;

    @PostMapping("/list")
    public ResponseVO<PageResultVO<AdminRefundInfoVO>> list(@RequestBody(required = false) AdminRefundQueryDTO dto) {
        return ResponseVO.success(adminOrderManageService.loadRefundList(dto));
    }

    @GetMapping("/detail/{refundId}")
    public ResponseVO<AdminRefundInfoVO> detail(@PathVariable String refundId) {
        return ResponseVO.success(adminOrderManageService.getRefundDetail(refundId));
    }

    @PostMapping("/approve/{refundId}")
    public ResponseVO<Void> approve(@PathVariable String refundId) {
        adminOrderManageService.approveRefund(refundId);
        return ResponseVO.success();
    }

    @PostMapping("/reject/{refundId}")
    public ResponseVO<Void> reject(@PathVariable String refundId) {
        adminOrderManageService.rejectRefund(refundId);
        return ResponseVO.success();
    }
}
