package com.smartMall.service;

import com.smartMall.entities.dto.AdminOrderQueryDTO;
import com.smartMall.entities.dto.AdminRefundQueryDTO;
import com.smartMall.entities.dto.AdminShipOrderDTO;
import com.smartMall.entities.vo.AdminOrderListVO;
import com.smartMall.entities.vo.AdminRefundInfoVO;
import com.smartMall.entities.vo.OrderDetailVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ShippingInfoVO;

/**
 * Admin order manage service.
 */
public interface AdminOrderManageService {

    PageResultVO<AdminOrderListVO> loadOrderList(AdminOrderQueryDTO dto);

    OrderDetailVO getOrderDetail(String orderId);

    ShippingInfoVO shipOrder(AdminShipOrderDTO dto);

    ShippingInfoVO getShippingDetail(String orderId);

    PageResultVO<AdminRefundInfoVO> loadRefundList(AdminRefundQueryDTO dto);

    AdminRefundInfoVO getRefundDetail(String refundId);

    void approveRefund(String refundId);

    void rejectRefund(String refundId);
}
