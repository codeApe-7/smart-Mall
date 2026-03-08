package com.smartMall.controller;

import com.smartMall.annotation.AdminAuditLog;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.smartMall.entities.dto.AdminOrderQueryDTO;
import com.smartMall.entities.dto.AdminShipOrderDTO;
import com.smartMall.entities.vo.AdminOrderListVO;
import com.smartMall.entities.vo.OrderDetailVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.entities.vo.ShippingInfoVO;
import com.smartMall.entities.enums.AdminOperationTypeEnum;
import com.smartMall.service.AdminOrderManageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin order manage controller.
 */
@RestController
@RequestMapping("/order")
@SaCheckPermission("order:manage")
public class OrderManageController {

    @Resource
    private AdminOrderManageService adminOrderManageService;

    @PostMapping("/list")
    public ResponseVO<PageResultVO<AdminOrderListVO>> list(@RequestBody(required = false) AdminOrderQueryDTO dto) {
        return ResponseVO.success(adminOrderManageService.loadOrderList(dto));
    }

    @GetMapping("/detail/{orderId}")
    public ResponseVO<OrderDetailVO> detail(@PathVariable String orderId) {
        return ResponseVO.success(adminOrderManageService.getOrderDetail(orderId));
    }

    @PostMapping("/ship")
    @AdminAuditLog(value = "后台订单发货", type = AdminOperationTypeEnum.ORDER)
    public ResponseVO<ShippingInfoVO> ship(@RequestBody @Valid AdminShipOrderDTO dto) {
        return ResponseVO.success(adminOrderManageService.shipOrder(dto));
    }

    @GetMapping("/shipping/{orderId}")
    public ResponseVO<ShippingInfoVO> shipping(@PathVariable String orderId) {
        return ResponseVO.success(adminOrderManageService.getShippingDetail(orderId));
    }
}


