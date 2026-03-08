package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.ProductReview;
import com.smartMall.entities.dto.ReviewQueryDTO;
import com.smartMall.entities.dto.ReviewSubmitDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductReviewVO;

import java.util.List;

/**
 * 商品评价 Service。
 */
public interface ProductReviewService extends IService<ProductReview> {

    List<ProductReviewVO> submitReview(ReviewSubmitDTO dto);

    List<ProductReviewVO> getOrderReviews(String userId, String orderId);

    PageResultVO<ProductReviewVO> getProductReviews(ReviewQueryDTO dto);
}
