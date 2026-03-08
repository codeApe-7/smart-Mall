package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.RefundInfo;
import com.smartMall.entities.dto.RefundApplyDTO;
import com.smartMall.entities.dto.RefundAuditDTO;
import com.smartMall.entities.enums.OrderStatusEnum;
import com.smartMall.entities.enums.RefundStatusEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.RefundInfoVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.mapper.RefundInfoMapper;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.RefundInfoService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * 退款 Service 实现。
 */
@Service
@Slf4j
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo>
        implements RefundInfoService {

    private static final int REFUND_NO_SUFFIX_LENGTH = 6;
    private static final DateTimeFormatter REFUND_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private OrderInfoService orderInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundInfoVO applyRefund(RefundApplyDTO dto) {
        OrderInfo orderInfo = orderInfoService.getUserOrder(dto.getUserId(), dto.getOrderId());
        if (!OrderStatusEnum.PAID.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support refund");
        }
        RefundInfo existingRefund = findPendingRefund(orderInfo.getOrderId());
        if (existingRefund != null) {
            log.info("reuse pending refund, userId={}, orderId={}, refundNo={}",
                    dto.getUserId(), dto.getOrderId(), existingRefund.getRefundNo());
            return buildRefundInfoVO(existingRefund);
        }
        Date now = new Date();
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setRefundId(StringTools.getRandomNumber(LENGTH_32));
        refundInfo.setRefundNo(generateRefundNo());
        refundInfo.setOrderId(orderInfo.getOrderId());
        refundInfo.setOrderNo(orderInfo.getOrderNo());
        refundInfo.setUserId(orderInfo.getUserId());
        refundInfo.setRefundAmount(orderInfo.getTotalAmount());
        refundInfo.setRefundReason(dto.getRefundReason());
        refundInfo.setRefundStatus(RefundStatusEnum.PENDING.getStatus());
        refundInfo.setCreateTime(now);
        refundInfo.setUpdateTime(now);
        this.save(refundInfo);

        orderInfoService.markOrderRefundRequested(orderInfo.getOrderId(), now);

        log.info("apply refund success, userId={}, orderId={}, refundNo={}",
                dto.getUserId(), dto.getOrderId(), refundInfo.getRefundNo());
        return buildRefundInfoVO(refundInfo);
    }

    @Override
    public RefundInfoVO getRefundDetail(String userId, String orderId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "userId and orderId are required");
        }
        orderInfoService.getUserOrder(userId, orderId);
        RefundInfo refundInfo = this.getOne(new LambdaQueryWrapper<RefundInfo>()
                .eq(RefundInfo::getOrderId, orderId)
                .eq(RefundInfo::getUserId, userId)
                .orderByDesc(RefundInfo::getCreateTime)
                .last("limit 1"));
        if (refundInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "refund record not found");
        }
        return buildRefundInfoVO(refundInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(RefundAuditDTO dto) {
        RefundInfo refundInfo = getRefundById(dto.getRefundId());
        validateRefundOwner(refundInfo, dto.getUserId());
        if (!RefundStatusEnum.PENDING.getStatus().equals(refundInfo.getRefundStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "refund status does not support approval");
        }
        Date now = new Date();
        refundInfo.setRefundStatus(RefundStatusEnum.APPROVED.getStatus());
        refundInfo.setApproveTime(now);
        refundInfo.setUpdateTime(now);
        this.updateById(refundInfo);

        orderInfoService.markOrderRefunded(refundInfo.getOrderId(), now);

        log.info("approve refund success, refundId={}, orderId={}", dto.getRefundId(), refundInfo.getOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(RefundAuditDTO dto) {
        RefundInfo refundInfo = getRefundById(dto.getRefundId());
        validateRefundOwner(refundInfo, dto.getUserId());
        if (!RefundStatusEnum.PENDING.getStatus().equals(refundInfo.getRefundStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "refund status does not support rejection");
        }
        Date now = new Date();
        refundInfo.setRefundStatus(RefundStatusEnum.REJECTED.getStatus());
        refundInfo.setApproveTime(now);
        refundInfo.setUpdateTime(now);
        this.updateById(refundInfo);

        orderInfoService.revertOrderFromRefund(refundInfo.getOrderId());

        log.info("reject refund, refundId={}, orderId={}", dto.getRefundId(), refundInfo.getOrderId());
    }

    private RefundInfo findPendingRefund(String orderId) {
        return this.getOne(new LambdaQueryWrapper<RefundInfo>()
                .eq(RefundInfo::getOrderId, orderId)
                .eq(RefundInfo::getRefundStatus, RefundStatusEnum.PENDING.getStatus())
                .orderByDesc(RefundInfo::getCreateTime)
                .last("limit 1"));
    }

    private RefundInfo getRefundById(String refundId) {
        if (StringTools.isEmpty(refundId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "refundId is required");
        }
        RefundInfo refundInfo = this.getById(refundId);
        if (refundInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "refund record not found");
        }
        return refundInfo;
    }

    private void validateRefundOwner(RefundInfo refundInfo, String userId) {
        if (StringTools.isEmpty(userId) || !userId.equals(refundInfo.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "refund does not belong to user");
        }
    }

    private RefundInfoVO buildRefundInfoVO(RefundInfo refundInfo) {
        RefundInfoVO vo = new RefundInfoVO();
        vo.setRefundId(refundInfo.getRefundId());
        vo.setRefundNo(refundInfo.getRefundNo());
        vo.setOrderId(refundInfo.getOrderId());
        vo.setOrderNo(refundInfo.getOrderNo());
        vo.setRefundAmount(refundInfo.getRefundAmount());
        vo.setRefundReason(refundInfo.getRefundReason());
        vo.setRefundStatus(refundInfo.getRefundStatus());
        vo.setRefundStatusDesc(getRefundStatusDesc(refundInfo.getRefundStatus()));
        vo.setCreateTime(refundInfo.getCreateTime());
        vo.setApproveTime(refundInfo.getApproveTime());
        return vo;
    }

    private String getRefundStatusDesc(Integer refundStatus) {
        RefundStatusEnum refundStatusEnum = RefundStatusEnum.getByStatus(refundStatus);
        return refundStatusEnum == null ? "unknown" : refundStatusEnum.getDesc();
    }

    private String generateRefundNo() {
        return "R" + LocalDateTime.now().format(REFUND_NO_TIME_FORMATTER) + StringTools.getRandomNumber(REFUND_NO_SUFFIX_LENGTH);
    }
}
