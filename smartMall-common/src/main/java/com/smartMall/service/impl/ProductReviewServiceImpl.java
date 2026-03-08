package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.OrderItem;
import com.smartMall.entities.domain.ProductReview;
import com.smartMall.entities.dto.ReviewItemDTO;
import com.smartMall.entities.dto.ReviewQueryDTO;
import com.smartMall.entities.dto.ReviewSubmitDTO;
import com.smartMall.entities.enums.OrderStatusEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductReviewVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.mapper.ProductReviewMapper;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.OrderItemService;
import com.smartMall.service.ProductReviewService;
import com.smartMall.service.UserPreferenceRefreshTrigger;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * 商品评价 Service 实现。
 */
@Service
@Slf4j
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview>
        implements ProductReviewService {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private UserPreferenceRefreshTrigger userPreferenceRefreshTrigger;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ProductReviewVO> submitReview(ReviewSubmitDTO dto) {
        OrderInfo orderInfo = orderInfoService.getUserOrder(dto.getUserId(), dto.getOrderId());
        if (!OrderStatusEnum.RECEIVED.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "order status does not support review");
        }

        List<OrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, dto.getOrderId()));
        Map<String, OrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, item -> item));

        Set<String> existingItemIds = this.list(new LambdaQueryWrapper<ProductReview>()
                        .eq(ProductReview::getOrderId, dto.getOrderId())
                        .eq(ProductReview::getUserId, dto.getUserId()))
                .stream()
                .map(ProductReview::getItemId)
                .collect(Collectors.toSet());

        Date now = new Date();
        List<ProductReview> newReviews = new ArrayList<>();
        for (ReviewItemDTO reviewItem : dto.getReviews()) {
            if (existingItemIds.contains(reviewItem.getItemId())) {
                throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED,
                        "order item already reviewed: " + reviewItem.getItemId());
            }
            OrderItem orderItem = orderItemMap.get(reviewItem.getItemId());
            if (orderItem == null) {
                throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST,
                        "order item not found: " + reviewItem.getItemId());
            }
            if (!orderItem.getProductId().equals(reviewItem.getProductId())) {
                throw new BusinessException(ResponseCodeEnum.PARAM_ERROR,
                        "productId mismatch for item: " + reviewItem.getItemId());
            }

            ProductReview review = new ProductReview();
            review.setReviewId(StringTools.getRandomNumber(LENGTH_32));
            review.setOrderId(dto.getOrderId());
            review.setItemId(reviewItem.getItemId());
            review.setProductId(reviewItem.getProductId());
            review.setUserId(dto.getUserId());
            review.setRating(reviewItem.getRating());
            review.setContent(reviewItem.getContent());
            review.setCreateTime(now);
            newReviews.add(review);
        }

        this.saveBatch(newReviews);

        long totalReviewed = existingItemIds.size() + newReviews.size();
        if (totalReviewed >= orderItems.size()) {
            orderInfoService.markOrderCompleted(orderInfo.getOrderId(), now);
            log.info("all items reviewed, order completed, orderId={}", dto.getOrderId());
        }

        log.info("submit review success, userId={}, orderId={}, count={}",
                dto.getUserId(), dto.getOrderId(), newReviews.size());
        userPreferenceRefreshTrigger.refreshUserPreferenceAsync(dto.getUserId(), "review_submit");
        return newReviews.stream().map(r -> buildReviewVO(r, orderItemMap)).toList();
    }

    @Override
    public List<ProductReviewVO> getOrderReviews(String userId, String orderId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(orderId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "userId and orderId are required");
        }
        orderInfoService.getUserOrder(userId, orderId);

        List<ProductReview> reviews = this.list(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getOrderId, orderId)
                .eq(ProductReview::getUserId, userId)
                .orderByAsc(ProductReview::getCreateTime));

        List<OrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        Map<String, OrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, item -> item));

        return reviews.stream().map(r -> buildReviewVO(r, orderItemMap)).toList();
    }

    @Override
    public PageResultVO<ProductReviewVO> getProductReviews(ReviewQueryDTO dto) {
        if (StringTools.isEmpty(dto.getProductId())) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "productId is required");
        }
        LambdaQueryWrapper<ProductReview> queryWrapper = new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getProductId, dto.getProductId())
                .orderByDesc(ProductReview::getCreateTime);
        Page<ProductReview> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        this.page(page, queryWrapper);
        if (page.getRecords().isEmpty()) {
            return PageResultVO.empty(dto.getPageNo(), dto.getPageSize());
        }
        List<ProductReviewVO> records = page.getRecords().stream()
                .map(this::buildSimpleReviewVO)
                .toList();
        return new PageResultVO<>(dto.getPageNo(), dto.getPageSize(), page.getTotal(), records);
    }

    private ProductReviewVO buildReviewVO(ProductReview review, Map<String, OrderItem> orderItemMap) {
        ProductReviewVO vo = new ProductReviewVO();
        vo.setReviewId(review.getReviewId());
        vo.setOrderId(review.getOrderId());
        vo.setItemId(review.getItemId());
        vo.setProductId(review.getProductId());
        vo.setUserId(review.getUserId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreateTime(review.getCreateTime());
        OrderItem orderItem = orderItemMap.get(review.getItemId());
        if (orderItem != null) {
            vo.setProductName(orderItem.getProductName());
        }
        return vo;
    }

    private ProductReviewVO buildSimpleReviewVO(ProductReview review) {
        ProductReviewVO vo = new ProductReviewVO();
        vo.setReviewId(review.getReviewId());
        vo.setOrderId(review.getOrderId());
        vo.setItemId(review.getItemId());
        vo.setProductId(review.getProductId());
        vo.setUserId(review.getUserId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreateTime(review.getCreateTime());
        return vo;
    }
}
