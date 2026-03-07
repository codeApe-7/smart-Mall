package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.PaymentInfo;
import com.smartMall.entities.dto.PaymentCallbackDTO;
import com.smartMall.entities.dto.PaymentSubmitDTO;
import com.smartMall.entities.vo.PaymentSubmitVO;

/**
 * Payment service.
 */
public interface PaymentInfoService extends IService<PaymentInfo> {

    PaymentSubmitVO submitPayment(PaymentSubmitDTO dto);

    void handleCallback(PaymentCallbackDTO dto);
}
