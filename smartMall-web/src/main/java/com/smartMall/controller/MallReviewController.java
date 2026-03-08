package com.smartMall.controller;

import com.smartMall.entities.dto.ReviewQueryDTO;
import com.smartMall.entities.dto.ReviewSubmitDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductReviewVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.ProductReviewService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端商品评价控制器。
 */
@Slf4j
@RestController
@RequestMapping("/review")
public class MallReviewController {

    @Resource
    private ProductReviewService productReviewService;

    @PostMapping("/submit")
    public ResponseVO<List<ProductReviewVO>> submit(@RequestBody @Valid ReviewSubmitDTO dto) {
        log.info("web submit review, userId={}, orderId={}", dto.getUserId(), dto.getOrderId());
        return ResponseVO.success(productReviewService.submitReview(dto));
    }

    @GetMapping("/orderReviews")
    public ResponseVO<List<ProductReviewVO>> orderReviews(@RequestParam String userId, @RequestParam String orderId) {
        log.info("web load order reviews, userId={}, orderId={}", userId, orderId);
        return ResponseVO.success(productReviewService.getOrderReviews(userId, orderId));
    }

    @PostMapping("/productReviews")
    public ResponseVO<PageResultVO<ProductReviewVO>> productReviews(@RequestBody @Valid ReviewQueryDTO dto) {
        log.info("web load product reviews, productId={}", dto.getProductId());
        return ResponseVO.success(productReviewService.getProductReviews(dto));
    }
}
