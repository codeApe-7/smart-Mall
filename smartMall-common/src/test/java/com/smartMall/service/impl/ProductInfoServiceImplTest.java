package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.ProductSku;
import com.smartMall.entities.domain.SysCategory;
import com.smartMall.entities.domain.UserPreference;
import com.smartMall.entities.vo.UserPreferenceVO;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.enums.ProductStatusEnum;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProductInfoServiceImpl 单元测试。
 */
class ProductInfoServiceImplTest {

    @Test
    void loadVisibleProductListShouldForceOnSaleStatus() {
        ProductInfoServiceImpl service = spy(new ProductInfoServiceImpl());
        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setStatus(ProductStatusEnum.OFF_SALE.getStatus());
        PageResultVO<ProductInfoListVO> expected = PageResultVO.empty(1, 10);
        doReturn(expected).when(service).loadProductList(any(ProductQueryDTO.class));

        PageResultVO<ProductInfoListVO> actual = service.loadVisibleProductList(queryDTO);

        assertSame(expected, actual);
        assertEquals(ProductStatusEnum.ON_SALE.getStatus(), queryDTO.getStatus());
    }

    @Test
    void loadRecommendProductsShouldUseVisibleRecommendQuery() {
        ProductInfoServiceImpl service = spy(new ProductInfoServiceImpl());
        List<ProductInfoListVO> records = List.of(new ProductInfoListVO());
        PageResultVO<ProductInfoListVO> expected = new PageResultVO<>(1, 20, 1L, records);
        doReturn(expected).when(service).loadProductList(any(ProductQueryDTO.class));

        List<ProductInfoListVO> actual = service.loadRecommendProducts(99);

        assertSame(records, actual);
    }

    @Test
    void getVisibleProductDetailShouldReturnOnSaleProduct() {
        ProductInfoServiceImpl service = spy(new ProductInfoServiceImpl());
        ProductInfo productInfo = new ProductInfo();
        productInfo.setStatus(ProductStatusEnum.ON_SALE.getStatus());
        ProductInfoDetailVo detailVo = new ProductInfoDetailVo();
        detailVo.setProductInfo(productInfo);
        doReturn(detailVo).when(service).getProductDetail("p1001");

        ProductInfoDetailVo actual = service.getVisibleProductDetail("p1001");

        assertSame(detailVo, actual);
    }

    @Test
    void getVisibleProductDetailShouldRejectOffSaleProduct() {
        ProductInfoServiceImpl service = spy(new ProductInfoServiceImpl());
        ProductInfo productInfo = new ProductInfo();
        productInfo.setStatus(ProductStatusEnum.OFF_SALE.getStatus());
        ProductInfoDetailVo detailVo = new ProductInfoDetailVo();
        detailVo.setProductInfo(productInfo);
        doReturn(detailVo).when(service).getProductDetail("p1002");

        assertThrows(BusinessException.class, () -> service.getVisibleProductDetail("p1002"));
    }

    @Test
    void loadPersonalizedRecommendProductsShouldRefreshPreferenceWhenMissing() {
        ProductInfoServiceImpl service = spy(new ProductInfoServiceImpl());
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId("p1001");
        productInfo.setProductName("Phone");
        productInfo.setCategoryId("c1");
        productInfo.setPCategoryId("pc1");
        productInfo.setStatus(ProductStatusEnum.ON_SALE.getStatus());

        ProductSku sku = new ProductSku();
        sku.setProductId("p1001");
        sku.setStock(9);

        SysCategory pCategory = new SysCategory();
        pCategory.setCategoryId("pc1");
        pCategory.setCategoryName("数码");
        SysCategory category = new SysCategory();
        category.setCategoryId("c1");
        category.setCategoryName("手机");

        UserPreference preference = new UserPreference();
        preference.setUserId("u1001");
        preference.setFavoriteCategoryIds("c1");

        UserPreferenceVO refreshResult = new UserPreferenceVO();
        com.smartMall.service.UserPreferenceService userPreferenceService = mock(com.smartMall.service.UserPreferenceService.class);
        com.smartMall.service.ProductSkuService productSkuService = mock(com.smartMall.service.ProductSkuService.class);
        com.smartMall.service.SysCategoryService sysCategoryService = mock(com.smartMall.service.SysCategoryService.class);
        com.smartMall.entities.config.UserPreferenceProperties preferenceProperties =
                new com.smartMall.entities.config.UserPreferenceProperties();
        preferenceProperties.setDefaultRecommendLimit(1);

        ReflectionTestUtils.setField(service, "userPreferenceService", userPreferenceService);
        ReflectionTestUtils.setField(service, "productSkuService", productSkuService);
        ReflectionTestUtils.setField(service, "sysCategoryService", sysCategoryService);
        ReflectionTestUtils.setField(service, "userPreferenceProperties", preferenceProperties);

        when(userPreferenceService.getOne(any())).thenReturn(null, preference);
        when(userPreferenceService.refreshUserPreference("u1001")).thenReturn(refreshResult);
        when(productSkuService.list(any(Wrapper.class))).thenReturn(List.of(sku));
        when(sysCategoryService.listByIds(any())).thenReturn(List.of(pCategory, category));
        doAnswer(invocation -> {
            Object pageArg = invocation.getArgument(0);
            if (pageArg instanceof com.baomidou.mybatisplus.extension.plugins.pagination.Page<?> page) {
                ((com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProductInfo>) page).setRecords(List.of(productInfo));
                return page;
            }
            return pageArg;
        }).when(service).page(any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class), any());

        List<ProductInfoListVO> actual = service.loadPersonalizedRecommendProducts("u1001", null);

        assertFalse(actual.isEmpty());
        assertEquals("p1001", actual.get(0).getProductId());
        verify(userPreferenceService).refreshUserPreference("u1001");
    }
}
