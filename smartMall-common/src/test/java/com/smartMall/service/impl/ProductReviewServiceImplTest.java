package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.OrderItem;
import com.smartMall.entities.dto.ReviewItemDTO;
import com.smartMall.entities.dto.ReviewSubmitDTO;
import com.smartMall.entities.enums.OrderStatusEnum;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.OrderItemService;
import com.smartMall.service.UserPreferenceRefreshTrigger;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProductReviewServiceImpl unit tests.
 */
class ProductReviewServiceImplTest {

    @Test
    void submitReviewShouldTriggerPreferenceRefreshAfterSuccess() {
        ProductReviewServiceImpl service = spy(new ProductReviewServiceImpl());
        OrderInfoService orderInfoService = mock(OrderInfoService.class);
        OrderItemService orderItemService = mock(OrderItemService.class);
        UserPreferenceRefreshTrigger userPreferenceRefreshTrigger = mock(UserPreferenceRefreshTrigger.class);

        ReflectionTestUtils.setField(service, "orderInfoService", orderInfoService);
        ReflectionTestUtils.setField(service, "orderItemService", orderItemService);
        ReflectionTestUtils.setField(service, "userPreferenceRefreshTrigger", userPreferenceRefreshTrigger);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderId("o1001");
        orderInfo.setOrderStatus(OrderStatusEnum.RECEIVED.getStatus());
        when(orderInfoService.getUserOrder("u1001", "o1001")).thenReturn(orderInfo);

        OrderItem orderItem = new OrderItem();
        orderItem.setItemId("item1001");
        orderItem.setOrderId("o1001");
        orderItem.setProductId("p1001");
        orderItem.setProductName("Phone");
        when(orderItemService.list(any(Wrapper.class))).thenReturn(List.of(orderItem));

        doReturn(List.of()).when(service).list(any(Wrapper.class));
        doReturn(true).when(service).saveBatch(any());

        ReviewItemDTO reviewItemDTO = new ReviewItemDTO();
        reviewItemDTO.setItemId("item1001");
        reviewItemDTO.setProductId("p1001");
        reviewItemDTO.setRating(5);
        reviewItemDTO.setContent("nice");

        ReviewSubmitDTO submitDTO = new ReviewSubmitDTO();
        submitDTO.setUserId("u1001");
        submitDTO.setOrderId("o1001");
        submitDTO.setReviews(List.of(reviewItemDTO));

        assertEquals(1, service.submitReview(submitDTO).size());
        verify(orderInfoService).markOrderCompleted(any(), any());
        verify(userPreferenceRefreshTrigger).refreshUserPreferenceAsync("u1001", "review_submit");
    }
}
