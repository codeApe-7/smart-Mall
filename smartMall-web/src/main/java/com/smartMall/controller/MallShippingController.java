package com.smartMall.controller;

import com.smartMall.entities.dto.ConfirmReceiveDTO;
import com.smartMall.entities.dto.ShipOrderDTO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.entities.vo.ShippingInfoVO;
import com.smartMall.service.ShippingInfoService;
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
 * 用户端物流控制器。
 */
@Slf4j
@RestController
@RequestMapping("/shipping")
public class MallShippingController {

    @Resource
    private ShippingInfoService shippingInfoService;

    @PostMapping("/ship")
    public ResponseVO<ShippingInfoVO> ship(@RequestBody @Valid ShipOrderDTO dto) {
        log.info("web ship order, userId={}, orderId={}", dto.getUserId(), dto.getOrderId());
        return ResponseVO.success(shippingInfoService.shipOrder(dto));
    }

    @GetMapping("/detail")
    public ResponseVO<ShippingInfoVO> detail(@RequestParam String userId, @RequestParam String orderId) {
        log.info("web load shipping detail, userId={}, orderId={}", userId, orderId);
        return ResponseVO.success(shippingInfoService.getShippingDetail(userId, orderId));
    }

    @PostMapping("/confirmReceive")
    public ResponseVO<Void> confirmReceive(@RequestBody @Valid ConfirmReceiveDTO dto) {
        log.info("web confirm receive, userId={}, orderId={}", dto.getUserId(), dto.getOrderId());
        shippingInfoService.confirmReceive(dto);
        return ResponseVO.success();
    }
}
