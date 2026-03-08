package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.OrderItem;
import com.smartMall.entities.domain.ProductReview;
import com.smartMall.entities.dto.AdminReviewQueryDTO;
import com.smartMall.entities.dto.AdminReviewReplyDTO;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.AdminReviewInfoVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.AdminReviewManageService;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.OrderItemService;
import com.smartMall.service.ProductReviewService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Admin review manage service implementation.
 */
@Service
public class AdminReviewManageServiceImpl implements AdminReviewManageService {

    @Resource
    private ProductReviewService productReviewService;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private OrderInfoService orderInfoService;

    @Override
    public PageResultVO<AdminReviewInfoVO> loadReviewList(AdminReviewQueryDTO dto) {
        AdminReviewQueryDTO safeQuery = dto == null ? new AdminReviewQueryDTO() : dto;
        List<String> matchedOrderIds = resolveMatchedOrderIds(safeQuery.getOrderNo());
        if (StringTools.isNotEmpty(safeQuery.getOrderNo()) && matchedOrderIds.isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }

        LambdaQueryWrapper<ProductReview> queryWrapper = new LambdaQueryWrapper<ProductReview>()
                .eq(StringTools.isNotEmpty(safeQuery.getProductId()), ProductReview::getProductId, safeQuery.getProductId())
                .eq(StringTools.isNotEmpty(safeQuery.getUserId()), ProductReview::getUserId, safeQuery.getUserId())
                .eq(safeQuery.getRating() != null, ProductReview::getRating, safeQuery.getRating())
                .isNotNull(Boolean.TRUE.equals(safeQuery.getReplied()), ProductReview::getReplyTime)
                .isNull(Boolean.FALSE.equals(safeQuery.getReplied()), ProductReview::getReplyTime)
                .in(!matchedOrderIds.isEmpty(), ProductReview::getOrderId, matchedOrderIds)
                .orderByDesc(ProductReview::getCreateTime);
        Page<ProductReview> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        productReviewService.page(page, queryWrapper);
        if (page.getRecords().isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }

        Map<String, OrderItem> orderItemMap = loadOrderItemMap(page.getRecords());
        Map<String, OrderInfo> orderInfoMap = loadOrderInfoMap(page.getRecords());
        return new PageResultVO<>(safeQuery.getPageNo(), safeQuery.getPageSize(), page.getTotal(),
                page.getRecords().stream()
                        .map(review -> buildReviewInfoVO(review, orderItemMap, orderInfoMap))
                        .toList());
    }

    @Override
    public AdminReviewInfoVO getReviewDetail(String reviewId) {
        ProductReview review = getReviewById(reviewId);
        Map<String, OrderItem> orderItemMap = loadOrderItemMap(List.of(review));
        Map<String, OrderInfo> orderInfoMap = loadOrderInfoMap(List.of(review));
        return buildReviewInfoVO(review, orderItemMap, orderInfoMap);
    }

    @Override
    public void replyReview(AdminReviewReplyDTO dto) {
        ProductReview review = getReviewById(dto.getReviewId());
        review.setReplyContent(dto.getReplyContent().trim());
        review.setReplyTime(new Date());
        productReviewService.updateById(review);
    }

    @Override
    public void deleteReview(String reviewId) {
        ProductReview review = getReviewById(reviewId);
        productReviewService.removeById(review.getReviewId());
    }

    private List<String> resolveMatchedOrderIds(String orderNo) {
        if (StringTools.isEmpty(orderNo)) {
            return List.of();
        }
        return orderInfoService.list(new LambdaQueryWrapper<OrderInfo>()
                        .select(OrderInfo::getOrderId)
                        .like(OrderInfo::getOrderNo, orderNo))
                .stream()
                .map(OrderInfo::getOrderId)
                .distinct()
                .toList();
    }

    private Map<String, OrderItem> loadOrderItemMap(List<ProductReview> reviews) {
        List<String> itemIds = reviews.stream()
                .map(ProductReview::getItemId)
                .filter(StringTools::isNotEmpty)
                .distinct()
                .toList();
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        return orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getItemId, itemIds))
                .stream()
                .collect(Collectors.toMap(OrderItem::getItemId, Function.identity(), (left, right) -> left));
    }

    private Map<String, OrderInfo> loadOrderInfoMap(List<ProductReview> reviews) {
        List<String> orderIds = reviews.stream()
                .map(ProductReview::getOrderId)
                .filter(StringTools::isNotEmpty)
                .distinct()
                .toList();
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return orderInfoService.list(new LambdaQueryWrapper<OrderInfo>()
                        .select(OrderInfo::getOrderId, OrderInfo::getOrderNo)
                        .in(OrderInfo::getOrderId, orderIds))
                .stream()
                .collect(Collectors.toMap(OrderInfo::getOrderId, Function.identity(), (left, right) -> left));
    }

    private ProductReview getReviewById(String reviewId) {
        if (StringTools.isEmpty(reviewId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "reviewId is required");
        }
        ProductReview review = productReviewService.getById(reviewId);
        if (review == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "review not found");
        }
        return review;
    }

    private AdminReviewInfoVO buildReviewInfoVO(ProductReview review,
                                                Map<String, OrderItem> orderItemMap,
                                                Map<String, OrderInfo> orderInfoMap) {
        AdminReviewInfoVO vo = new AdminReviewInfoVO();
        vo.setReviewId(review.getReviewId());
        vo.setOrderId(review.getOrderId());
        vo.setItemId(review.getItemId());
        vo.setProductId(review.getProductId());
        vo.setUserId(review.getUserId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setReplyContent(review.getReplyContent());
        vo.setCreateTime(review.getCreateTime());
        vo.setReplyTime(review.getReplyTime());

        OrderItem orderItem = orderItemMap.get(review.getItemId());
        if (orderItem != null) {
            vo.setProductName(orderItem.getProductName());
        }
        OrderInfo orderInfo = orderInfoMap.get(review.getOrderId());
        if (orderInfo != null) {
            vo.setOrderNo(orderInfo.getOrderNo());
        }
        return vo;
    }
}
