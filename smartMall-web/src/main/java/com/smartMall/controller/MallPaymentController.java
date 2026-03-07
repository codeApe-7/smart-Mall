package com.smartMall.controller;

import com.smartMall.entities.dto.PaymentCallbackDTO;
import com.smartMall.entities.dto.PaymentSubmitDTO;
import com.smartMall.entities.vo.PaymentSubmitVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.PaymentInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mall payment controller.
 */
@Slf4j
@RestController
@RequestMapping("/payment")
public class MallPaymentController {

    @Resource
    private PaymentInfoService paymentInfoService;

    @PostMapping("/submit")
    public ResponseVO<PaymentSubmitVO> submit(@RequestBody @Valid PaymentSubmitDTO dto) {
        log.info("web submit payment, userId={}, orderId={}", dto.getUserId(), dto.getOrderId());
        return ResponseVO.success(paymentInfoService.submitPayment(dto));
    }

    @PostMapping("/callback")
    public ResponseVO<Void> callback(@RequestBody @Valid PaymentCallbackDTO dto) {
        log.info("web payment callback, paymentNo={}, callbackStatus={}", dto.getPaymentNo(), dto.getCallbackStatus());
        paymentInfoService.handleCallback(dto);
        return ResponseVO.success();
    }
}
