package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartMall.entities.domain.AssistantChatLog;
import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.dto.AssistantHistoryQueryDTO;
import com.smartMall.entities.vo.AssistantChatResponseVO;
import com.smartMall.entities.vo.OrderDetailVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.service.AssistantChatLogService;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.ProductInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MallAssistantServiceImpl unit tests.
 */
class MallAssistantServiceImplTest {

    private MallAssistantServiceImpl service;

    private ProductInfoService productInfoService;

    private OrderInfoService orderInfoService;

    private AssistantChatLogService assistantChatLogService;

    @BeforeEach
    void setUp() {
        service = new MallAssistantServiceImpl();
        productInfoService = mock(ProductInfoService.class);
        orderInfoService = mock(OrderInfoService.class);
        assistantChatLogService = mock(AssistantChatLogService.class);
        ReflectionTestUtils.setField(service, "productInfoService", productInfoService);
        ReflectionTestUtils.setField(service, "orderInfoService", orderInfoService);
        ReflectionTestUtils.setField(service, "assistantChatLogService", assistantChatLogService);
        when(assistantChatLogService.save(any(AssistantChatLog.class))).thenReturn(true);
    }

    @Test
    void chatShouldReturnRecommendProducts() {
        ProductInfoListVO product = new ProductInfoListVO();
        product.setProductId("p10001");
        when(productInfoService.loadRecommendProducts(4)).thenReturn(List.of(product));

        AssistantChatRequestDTO dto = new AssistantChatRequestDTO();
        dto.setUserId("u1");
        dto.setMessage("帮我推荐几款热销商品");

        AssistantChatResponseVO response = service.chat(dto);

        assertEquals("PRODUCT_RECOMMEND", response.getIntentType());
        assertEquals("p10001", response.getPayload().getProductPage().getRecords().get(0).getProductId());
        verify(assistantChatLogService).save(any(AssistantChatLog.class));
    }

    @Test
    void chatShouldSearchProductByKeyword() {
        ProductInfoListVO product = new ProductInfoListVO();
        product.setProductId("p20001");
        PageResultVO<ProductInfoListVO> pageResultVO = new PageResultVO<>(1, 5, 1L, List.of(product));
        when(productInfoService.loadVisibleProductList(any())).thenReturn(pageResultVO);

        AssistantChatRequestDTO dto = new AssistantChatRequestDTO();
        dto.setUserId("u1");
        dto.setMessage("帮我找手机");

        AssistantChatResponseVO response = service.chat(dto);

        assertEquals("PRODUCT_SEARCH", response.getIntentType());
        assertSame(pageResultVO, response.getPayload().getProductPage());
    }

    @Test
    void chatShouldReturnOrderDetail() {
        OrderDetailVO detailVO = new OrderDetailVO();
        detailVO.setOrderId("o10001");
        detailVO.setOrderNo("202603080001");
        when(orderInfoService.getOrderDetail("u1", "o10001")).thenReturn(detailVO);

        AssistantChatRequestDTO dto = new AssistantChatRequestDTO();
        dto.setUserId("u1");
        dto.setMessage("查看订单详情 o10001");

        AssistantChatResponseVO response = service.chat(dto);

        assertEquals("ORDER_DETAIL", response.getIntentType());
        assertEquals("o10001", response.getPayload().getOrderDetail().getOrderId());
    }

    @Test
    void chatShouldCancelOrder() {
        OrderDetailVO detailVO = new OrderDetailVO();
        detailVO.setOrderId("o10001");
        detailVO.setOrderNo("202603080002");
        detailVO.setOrderStatus(20);
        detailVO.setOrderStatusDesc("已取消");
        doNothing().when(orderInfoService).cancelOrder(any());
        when(orderInfoService.getOrderDetail("u1", "o10001")).thenReturn(detailVO);

        AssistantChatRequestDTO dto = new AssistantChatRequestDTO();
        dto.setUserId("u1");
        dto.setMessage("取消订单 o10001");

        AssistantChatResponseVO response = service.chat(dto);

        assertEquals("ORDER_CANCEL", response.getIntentType());
        assertEquals(20, response.getPayload().getOperation().getOrderStatus());
        verify(orderInfoService).cancelOrder(any());
    }

    @Test
    void loadHistoryShouldMapPageResult() {
        doAnswer(invocation -> {
            Page<AssistantChatLog> page = invocation.getArgument(0);
            AssistantChatLog chatLog = new AssistantChatLog();
            chatLog.setChatId("c1");
            chatLog.setSessionId("s1");
            chatLog.setUserId("u1");
            chatLog.setRequestText("帮我找手机");
            chatLog.setIntentType("PRODUCT_SEARCH");
            chatLog.setReplyText("为你找到商品");
            chatLog.setCreateTime(new Date());
            page.setRecords(List.of(chatLog));
            page.setTotal(1L);
            return page;
        }).when(assistantChatLogService).page(any(Page.class), any());

        AssistantHistoryQueryDTO dto = new AssistantHistoryQueryDTO();
        dto.setUserId("u1");

        PageResultVO<?> result = service.loadHistory(dto);

        assertEquals(1L, result.getTotalCount());
        assertNotNull(result.getRecords());
    }
}
