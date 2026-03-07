package com.smartMall.controller;

import com.smartMall.entities.dto.OrderCancelDTO;
import com.smartMall.entities.dto.OrderCreateDTO;
import com.smartMall.entities.dto.OrderPreviewDTO;
import com.smartMall.entities.dto.OrderQueryDTO;
import com.smartMall.entities.vo.OrderCreateVO;
import com.smartMall.entities.vo.OrderDetailVO;
import com.smartMall.entities.vo.OrderInfoListVO;
import com.smartMall.entities.vo.OrderPreviewVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.OrderInfoService;
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
 * 用户端订单控制器。
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class MallOrderController {

    @Resource
    private OrderInfoService orderInfoService;

    @PostMapping("/preview")
    public ResponseVO<OrderPreviewVO> preview(@RequestBody @Valid OrderPreviewDTO dto) {
        log.info("web preview order, userId={}, itemCount={}", dto.getUserId(), dto.getCartIds().size());
        return ResponseVO.success(orderInfoService.previewOrder(dto));
    }

    @PostMapping("/create")
    public ResponseVO<OrderCreateVO> create(@RequestBody @Valid OrderCreateDTO dto) {
        log.info("web create order, userId={}, itemCount={}", dto.getUserId(), dto.getCartIds().size());
        return ResponseVO.success(orderInfoService.createOrder(dto));
    }

    @PostMapping("/list")
    public ResponseVO<PageResultVO<OrderInfoListVO>> list(@RequestBody OrderQueryDTO dto) {
        OrderQueryDTO safeQuery = dto == null ? new OrderQueryDTO() : dto;
        log.info("web load orders, userId={}, pageNo={}, pageSize={}", safeQuery.getUserId(), safeQuery.getPageNo(), safeQuery.getPageSize());
        return ResponseVO.success(orderInfoService.loadOrderList(safeQuery));
    }

    @GetMapping("/detail")
    public ResponseVO<OrderDetailVO> detail(@RequestParam String userId, @RequestParam String orderId) {
        log.info("web load order detail, userId={}, orderId={}", userId, orderId);
        return ResponseVO.success(orderInfoService.getOrderDetail(userId, orderId));
    }

    @PostMapping("/cancel")
    public ResponseVO<Void> cancel(@RequestBody @Valid OrderCancelDTO dto) {
        orderInfoService.cancelOrder(dto);
        return ResponseVO.success();
    }
}
