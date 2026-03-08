package com.smartMall.entities.vo;

import lombok.Data;

/**
 * Assistant chat payload.
 */
@Data
public class AssistantChatPayloadVO {

    private PageResultVO<ProductInfoListVO> productPage;

    private ProductInfoDetailVo productDetail;

    private PageResultVO<OrderInfoListVO> orderPage;

    private OrderDetailVO orderDetail;

    private AssistantOperationVO operation;
}
