package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.dto.OrderCancelDTO;
import com.smartMall.entities.dto.OrderCreateDTO;
import com.smartMall.entities.dto.OrderPreviewDTO;
import com.smartMall.entities.dto.OrderQueryDTO;
import com.smartMall.entities.vo.OrderCreateVO;
import com.smartMall.entities.vo.OrderDetailVO;
import com.smartMall.entities.vo.OrderInfoListVO;
import com.smartMall.entities.vo.OrderPreviewVO;
import com.smartMall.entities.vo.PageResultVO;

import java.util.Date;

/**
 * 订单主表 Service。
 */
public interface OrderInfoService extends IService<OrderInfo> {

    OrderPreviewVO previewOrder(OrderPreviewDTO dto);

    OrderCreateVO createOrder(OrderCreateDTO dto);

    PageResultVO<OrderInfoListVO> loadOrderList(OrderQueryDTO dto);

    OrderDetailVO getOrderDetail(String userId, String orderId);

    void cancelOrder(OrderCancelDTO dto);

    OrderInfo getUserOrder(String userId, String orderId);

    void markOrderPaid(String orderId, Date payTime);

    void markOrderRefundRequested(String orderId, Date refundTime);

    void markOrderRefunded(String orderId, Date refundTime);

    void revertOrderFromRefund(String orderId);
}
