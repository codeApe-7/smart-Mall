package com.smartMall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartMall.entities.dto.CartAddDTO;
import com.smartMall.entities.dto.CartDeleteDTO;
import com.smartMall.entities.dto.CartQuantityUpdateDTO;
import com.smartMall.entities.vo.ShoppingCartVO;
import com.smartMall.exception.GlobalExceptionHandler;
import com.smartMall.service.ShoppingCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MallCartController 接口测试。
 */
class MallCartControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private ShoppingCartService shoppingCartService;

    @BeforeEach
    void setUp() {
        shoppingCartService = mock(ShoppingCartService.class);
        MallCartController controller = new MallCartController();
        ReflectionTestUtils.setField(controller, "shoppingCartService", shoppingCartService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listShouldReturnCartSummary() throws Exception {
        ShoppingCartVO shoppingCartVO = new ShoppingCartVO();
        shoppingCartVO.setItemCount(1);
        when(shoppingCartService.loadCart("u1")).thenReturn(shoppingCartVO);

        mockMvc.perform(get("/cart/list").param("userId", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.itemCount").value(1));
    }

    @Test
    void addShouldReturnSuccess() throws Exception {
        doNothing().when(shoppingCartService).addCartItem(any(CartAddDTO.class));
        CartAddDTO dto = new CartAddDTO();
        dto.setUserId("u1");
        dto.setProductId("p1");
        dto.setPropertyValueIdHash("sku1");
        dto.setQuantity(1);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void updateQuantityShouldReturnSuccess() throws Exception {
        doNothing().when(shoppingCartService).updateQuantity(any(CartQuantityUpdateDTO.class));
        CartQuantityUpdateDTO dto = new CartQuantityUpdateDTO();
        dto.setUserId("u1");
        dto.setCartId("c1");
        dto.setQuantity(2);

        mockMvc.perform(post("/cart/updateQuantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteShouldReturnSuccess() throws Exception {
        doNothing().when(shoppingCartService).deleteItems(any(CartDeleteDTO.class));
        CartDeleteDTO dto = new CartDeleteDTO();
        dto.setUserId("u1");
        dto.setCartIds(List.of("c1"));

        mockMvc.perform(post("/cart/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
