package com.smartMall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.exception.GlobalExceptionHandler;
import com.smartMall.service.ProductInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MallProductController 接口测试。
 */
class MallProductControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private ProductInfoService productInfoService;

    @BeforeEach
    void setUp() {
        productInfoService = mock(ProductInfoService.class);
        MallProductController controller = new MallProductController();
        ReflectionTestUtils.setField(controller, "productInfoService", productInfoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listShouldReturnVisibleProducts() throws Exception {
        ProductInfoListVO item = new ProductInfoListVO();
        item.setProductId("p1");
        item.setProductName("Phone");
        PageResultVO<ProductInfoListVO> pageResultVO = new PageResultVO<>(1, 10, 1L, List.of(item));
        when(productInfoService.loadVisibleProductList(any(ProductQueryDTO.class))).thenReturn(pageResultVO);

        mockMvc.perform(post("/product/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductQueryDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].productId").value("p1"));
    }

    @Test
    void recommendShouldReturnRecommendProducts() throws Exception {
        ProductInfoListVO item = new ProductInfoListVO();
        item.setProductId("p2");
        when(productInfoService.loadRecommendProducts(eq(4))).thenReturn(List.of(item));

        mockMvc.perform(get("/product/recommend").param("limit", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].productId").value("p2"));
    }

    @Test
    void recommendShouldReturnPersonalizedRecommendProductsWhenUserIdProvided() throws Exception {
        ProductInfoListVO item = new ProductInfoListVO();
        item.setProductId("p9");
        when(productInfoService.loadPersonalizedRecommendProducts("u1", 4)).thenReturn(List.of(item));

        mockMvc.perform(get("/product/recommend").param("userId", "u1").param("limit", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].productId").value("p9"));

        verify(productInfoService).loadPersonalizedRecommendProducts("u1", 4);
    }

    @Test
    void detailShouldReturnVisibleProductDetail() throws Exception {
        ProductInfoDetailVo detailVo = new ProductInfoDetailVo();
        when(productInfoService.getVisibleProductDetail("p3")).thenReturn(detailVo);

        mockMvc.perform(get("/product/detail/p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
