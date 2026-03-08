package com.smartMall.controller;

import com.smartMall.entities.dto.RefundApplyDTO;
import com.smartMall.entities.dto.RefundAuditDTO;
import com.smartMall.entities.vo.RefundInfoVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.RefundInfoService;
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
 * 用户端退款控制器。
 */
@Slf4j
@RestController
@RequestMapping("/refund")
public class MallRefundController {

    @Resource
    private RefundInfoService refundInfoService;

    @PostMapping("/apply")
    public ResponseVO<RefundInfoVO> apply(@RequestBody @Valid RefundApplyDTO dto) {
        log.info("web apply refund, userId={}, orderId={}", dto.getUserId(), dto.getOrderId());
        return ResponseVO.success(refundInfoService.applyRefund(dto));
    }

    @GetMapping("/detail")
    public ResponseVO<RefundInfoVO> detail(@RequestParam String userId, @RequestParam String orderId) {
        log.info("web load refund detail, userId={}, orderId={}", userId, orderId);
        return ResponseVO.success(refundInfoService.getRefundDetail(userId, orderId));
    }

    @PostMapping("/approve")
    public ResponseVO<Void> approve(@RequestBody @Valid RefundAuditDTO dto) {
        log.info("web approve refund, refundId={}, userId={}", dto.getRefundId(), dto.getUserId());
        refundInfoService.approveRefund(dto);
        return ResponseVO.success();
    }

    @PostMapping("/reject")
    public ResponseVO<Void> reject(@RequestBody @Valid RefundAuditDTO dto) {
        log.info("web reject refund, refundId={}, userId={}", dto.getRefundId(), dto.getUserId());
        refundInfoService.rejectRefund(dto);
        return ResponseVO.success();
    }
}
