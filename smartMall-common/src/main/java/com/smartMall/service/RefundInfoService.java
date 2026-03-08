package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.RefundInfo;
import com.smartMall.entities.dto.RefundApplyDTO;
import com.smartMall.entities.dto.RefundAuditDTO;
import com.smartMall.entities.vo.RefundInfoVO;

/**
 * 退款 Service。
 */
public interface RefundInfoService extends IService<RefundInfo> {

    RefundInfoVO applyRefund(RefundApplyDTO dto);

    RefundInfoVO getRefundDetail(String userId, String orderId);

    void approveRefund(RefundAuditDTO dto);

    void rejectRefund(RefundAuditDTO dto);
}
