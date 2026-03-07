package com.smartMall.service.impl;

import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.enums.ProductStatusEnum;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

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
}
