package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.ShippingInfo;
import com.smartMall.entities.dto.ConfirmReceiveDTO;
import com.smartMall.entities.dto.ShipOrderDTO;
import com.smartMall.entities.vo.ShippingInfoVO;

/**
 * 物流 Service。
 */
public interface ShippingInfoService extends IService<ShippingInfo> {

    ShippingInfoVO shipOrder(ShipOrderDTO dto);

    ShippingInfoVO getShippingDetail(String userId, String orderId);

    void confirmReceive(ConfirmReceiveDTO dto);
}
