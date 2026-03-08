package com.smartMall.entities.vo;

import lombok.Data;

import java.util.List;

/**
 * Assistant chat payload.
 */
@Data
public class AssistantChatPayloadVO {

    private PageResultVO<ProductInfoListVO> productPage;

    private ProductInfoDetailVo productDetail;

    private PageResultVO<OrderInfoListVO> orderPage;

    private OrderDetailVO orderDetail;

    private RefundInfoVO refundInfo;

    private ShippingInfoVO shippingInfo;

    private List<ProductReviewVO> orderReviews;

    private AssistantOperationVO operation;
}
