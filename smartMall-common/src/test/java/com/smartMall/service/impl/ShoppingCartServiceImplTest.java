package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.ProductPropertyValue;
import com.smartMall.entities.domain.ProductSku;
import com.smartMall.entities.domain.ShoppingCart;
import com.smartMall.entities.dto.CartAddDTO;
import com.smartMall.entities.dto.CartQuantityUpdateDTO;
import com.smartMall.entities.enums.ProductStatusEnum;
import com.smartMall.entities.vo.ShoppingCartVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductPropertyValueService;
import com.smartMall.service.ProductSkuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ShoppingCartServiceImpl 单元测试。
 */
class ShoppingCartServiceImplTest {

    private ShoppingCartServiceImpl service;

    private ProductInfoService productInfoService;

    private ProductSkuService productSkuService;

    private ProductPropertyValueService productPropertyValueService;

    @BeforeEach
    void setUp() {
        service = spy(new ShoppingCartServiceImpl());
        productInfoService = mock(ProductInfoService.class);
        productSkuService = mock(ProductSkuService.class);
        productPropertyValueService = mock(ProductPropertyValueService.class);
        ReflectionTestUtils.setField(service, "productInfoService", productInfoService);
        ReflectionTestUtils.setField(service, "productSkuService", productSkuService);
        ReflectionTestUtils.setField(service, "productPropertyValueService", productPropertyValueService);
    }

    @Test
    void addCartItemShouldCreateNewItem() {
        when(productInfoService.getById("p1")).thenReturn(buildProduct("p1", ProductStatusEnum.ON_SALE.getStatus()));
        when(productSkuService.getOne(any(LambdaQueryWrapper.class))).thenReturn(buildSku("p1", "sku1", 5, "pv1,pv2"));
        doReturn(null).when(service).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(service).save(any(ShoppingCart.class));

        CartAddDTO dto = new CartAddDTO();
        dto.setUserId("u1");
        dto.setProductId("p1");
        dto.setPropertyValueIdHash("sku1");
        dto.setQuantity(2);

        service.addCartItem(dto);

        verify(service).save(argThat(cart -> "u1".equals(cart.getUserId())
                && "p1".equals(cart.getProductId())
                && Integer.valueOf(2).equals(cart.getQuantity())
                && "pv1,pv2".equals(cart.getPropertyValueIds())));
    }

    @Test
    void addCartItemShouldMergeExistingItem() {
        when(productInfoService.getById("p1")).thenReturn(buildProduct("p1", ProductStatusEnum.ON_SALE.getStatus()));
        when(productSkuService.getOne(any(LambdaQueryWrapper.class))).thenReturn(buildSku("p1", "sku1", 5, "pv1,pv2"));
        ShoppingCart existing = new ShoppingCart();
        existing.setCartId("c1");
        existing.setUserId("u1");
        existing.setProductId("p1");
        existing.setPropertyValueIdHash("sku1");
        existing.setQuantity(1);
        doReturn(existing).when(service).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(service).updateById(any(ShoppingCart.class));

        CartAddDTO dto = new CartAddDTO();
        dto.setUserId("u1");
        dto.setProductId("p1");
        dto.setPropertyValueIdHash("sku1");
        dto.setQuantity(2);

        service.addCartItem(dto);

        assertEquals(3, existing.getQuantity());
        verify(service).updateById(existing);
    }

    @Test
    void loadCartShouldAssembleSummary() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartId("c1");
        cart.setUserId("u1");
        cart.setProductId("p1");
        cart.setPropertyValueIdHash("sku1");
        cart.setPropertyValueIds("pv1,pv2");
        cart.setQuantity(2);
        cart.setSelected(1);
        doReturn(List.of(cart)).when(service).list(any(LambdaQueryWrapper.class));

        when(productInfoService.listByIds(any())).thenReturn(List.of(buildProduct("p1", ProductStatusEnum.ON_SALE.getStatus())));
        when(productSkuService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(buildSku("p1", "sku1", 10, "pv1,pv2")));
        when(productPropertyValueService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildPropertyValue("p1", "pv1", "Red"),
                buildPropertyValue("p1", "pv2", "XL")));

        ShoppingCartVO shoppingCartVO = service.loadCart("u1");

        assertEquals(1, shoppingCartVO.getItemCount());
        assertEquals(2, shoppingCartVO.getTotalQuantity());
        assertEquals(new BigDecimal("199.98"), shoppingCartVO.getSelectedAmount());
        assertEquals("Red / XL", shoppingCartVO.getItems().getFirst().getSkuPropertyText());
    }

    @Test
    void updateQuantityShouldRejectWhenExceedStock() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartId("c1");
        cart.setUserId("u1");
        cart.setProductId("p1");
        cart.setPropertyValueIdHash("sku1");
        doReturn(cart).when(service).getOne(any(LambdaQueryWrapper.class));
        when(productSkuService.getOne(any(LambdaQueryWrapper.class))).thenReturn(buildSku("p1", "sku1", 1, "pv1,pv2"));

        CartQuantityUpdateDTO dto = new CartQuantityUpdateDTO();
        dto.setUserId("u1");
        dto.setCartId("c1");
        dto.setQuantity(2);

        assertThrows(BusinessException.class, () -> service.updateQuantity(dto));
    }

    private ProductInfo buildProduct(String productId, Integer status) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId(productId);
        productInfo.setProductName("Phone");
        productInfo.setCover("cover.png");
        productInfo.setStatus(status);
        return productInfo;
    }

    private ProductSku buildSku(String productId, String skuHash, Integer stock, String propertyValueIds) {
        ProductSku productSku = new ProductSku();
        productSku.setProductId(productId);
        productSku.setPropertyValueIdHash(skuHash);
        productSku.setPropertyValueIds(propertyValueIds);
        productSku.setPrice(new BigDecimal("99.99"));
        productSku.setStock(stock);
        return productSku;
    }

    private ProductPropertyValue buildPropertyValue(String productId, String propertyValueId, String propertyValue) {
        ProductPropertyValue value = new ProductPropertyValue();
        value.setProductId(productId);
        value.setPropertyValueId(propertyValueId);
        value.setPropertyValue(propertyValue);
        return value;
    }
}
