package com.smartMall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.ProductPropertyValue;
import com.smartMall.entities.domain.ProductSku;
import com.smartMall.entities.domain.SysCategory;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.dto.ProductSaveDTO;
import com.smartMall.entities.enums.ProductStatusEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.entities.vo.ProductInfoVO;
import com.smartMall.entities.vo.ProductPropertyVO;
import com.smartMall.entities.vo.ProductPropertyValueVO;
import com.smartMall.entities.vo.ProductSkuVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.mapper.ProductInfoMapper;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductPropertyValueService;
import com.smartMall.service.ProductSkuService;
import com.smartMall.service.SysCategoryService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * @author 15712
 * @description 针对表【product_info(商品信息)】的数据库操作Service实现
 * @createDate 2026-02-13 15:52:46
 */
@Service
@Slf4j
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfo>
        implements ProductInfoService {

    private static final int DEFAULT_RECOMMEND_LIMIT = 6;
    private static final int MAX_RECOMMEND_LIMIT = 20;
    private static final int RECOMMEND_PRODUCT = 1;

    @Resource
    private ProductPropertyValueService productPropertyValueService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private SysCategoryService sysCategoryService;

    @Override
    public PageResultVO<ProductInfoListVO> loadProductList(ProductQueryDTO queryDTO) {
        ProductQueryDTO safeQuery = queryDTO == null ? new ProductQueryDTO() : queryDTO;
        LambdaQueryWrapper<ProductInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringTools.isNotEmpty(safeQuery.getProductName()),
                        ProductInfo::getProductName, safeQuery.getProductName())
                .and(StringTools.isNotEmpty(safeQuery.getCategoryIdOrPCategoryId()),
                        wrapper -> wrapper.eq(ProductInfo::getCategoryId, safeQuery.getCategoryIdOrPCategoryId())
                                .or()
                                .eq(ProductInfo::getPCategoryId, safeQuery.getCategoryIdOrPCategoryId()))
                .eq(safeQuery.getCommendType() != null,
                        ProductInfo::getCommendType, safeQuery.getCommendType())
                .eq(safeQuery.getStatus() != null,
                        ProductInfo::getStatus, safeQuery.getStatus())
                .orderByDesc(ProductInfo::getCreateTime);

        Page<ProductInfo> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        this.page(page, queryWrapper);

        List<ProductInfo> productList = page.getRecords();
        if (productList.isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }

        Set<String> categoryIds = new HashSet<>();
        productList.forEach(product -> {
            if (product.getCategoryId() != null) {
                categoryIds.add(product.getCategoryId());
            }
            if (product.getPCategoryId() != null) {
                categoryIds.add(product.getPCategoryId());
            }
        });

        Map<String, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<SysCategory> categories = sysCategoryService.listByIds(categoryIds);
            categoryNameMap = categories.stream().collect(Collectors.toMap(
                    SysCategory::getCategoryId,
                    SysCategory::getCategoryName,
                    (left, right) -> left));
        }

        List<String> productIds = productList.stream()
                .map(ProductInfo::getProductId)
                .toList();
        List<ProductSku> allSkus = productSkuService.list(
                new LambdaQueryWrapper<ProductSku>().in(ProductSku::getProductId, productIds));
        Map<String, List<ProductSku>> skuGroupMap = allSkus.stream()
                .collect(Collectors.groupingBy(ProductSku::getProductId));

        Map<String, String> finalCategoryNameMap = categoryNameMap;
        List<ProductInfoListVO> voList = productList.stream().map(product -> {
            ProductInfoListVO productInfoListVO = new ProductInfoListVO();
            BeanUtils.copyProperties(product, productInfoListVO);
            String pName = finalCategoryNameMap.getOrDefault(product.getPCategoryId(), "");
            String cName = finalCategoryNameMap.getOrDefault(product.getCategoryId(), "");
            productInfoListVO.setCategoryName(pName + "/" + cName);

            List<ProductSku> skus = skuGroupMap.getOrDefault(product.getProductId(), List.of());
            productInfoListVO.setSkuCount(skus.size());
            productInfoListVO.setTotalStock(skus.stream()
                    .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                    .sum());
            return productInfoListVO;
        }).toList();

        return new PageResultVO<>(safeQuery.getPageNo(), safeQuery.getPageSize(), page.getTotal(), voList);
    }

    @Override
    public PageResultVO<ProductInfoListVO> loadVisibleProductList(ProductQueryDTO queryDTO) {
        ProductQueryDTO visibleQuery = queryDTO == null ? new ProductQueryDTO() : queryDTO;
        visibleQuery.setStatus(ProductStatusEnum.ON_SALE.getStatus());
        log.info("load visible products, pageNo={}, pageSize={}, categoryId={}, commendType={}",
                visibleQuery.getPageNo(), visibleQuery.getPageSize(),
                visibleQuery.getCategoryIdOrPCategoryId(), visibleQuery.getCommendType());
        return loadProductList(visibleQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(ProductSaveDTO productSaveDTO) {
        ProductInfoVO productInfoVO = productSaveDTO.getProductInfo();
        ProductInfo productInfo = new ProductInfo();
        BeanUtils.copyProperties(productInfoVO, productInfo);

        List<ProductPropertyValue> productPropertyValueList = convertPropertyValues(
                productSaveDTO.getProductPropertyList());
        List<ProductSku> skuList = convertSkus(productSaveDTO.getSkuList());

        boolean isAdd = StringTools.isEmpty(productInfo.getProductId());
        if (isAdd) {
            productInfo.setProductId(StringTools.getRandomNumber(LENGTH_32));
        }

        productPropertyValueList.forEach(propertyValue -> propertyValue.setProductId(productInfo.getProductId()));
        skuList.forEach(sku -> sku.setProductId(productInfo.getProductId()));

        productInfo.setStatus(null);
        productInfo.setCommendType(null);

        Optional<ProductSku> minPrice = skuList.stream()
                .min((left, right) -> left.getPrice().compareTo(right.getPrice()));
        Optional<ProductSku> maxPrice = skuList.stream()
                .max((left, right) -> left.getPrice().compareTo(right.getPrice()));
        productInfo.setMinPrice(minPrice.get().getPrice());
        productInfo.setMaxPrice(maxPrice.get().getPrice());

        if (isAdd) {
            productInfo.setCreateTime(new Date());
            productInfo.setStatus(ProductStatusEnum.OFF_SALE.getStatus());

            this.baseMapper.insert(productInfo);
            productPropertyValueService.saveBatch(productPropertyValueList);
            productSkuService.saveBatch(skuList);
            return;
        }

        this.baseMapper.updateById(productInfo);
        productPropertyValueService.remove(new LambdaQueryWrapper<ProductPropertyValue>()
                .eq(ProductPropertyValue::getProductId, productInfo.getProductId()));
        productSkuService.remove(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productInfo.getProductId()));

        if (!productPropertyValueList.isEmpty()) {
            productPropertyValueService.saveBatch(productPropertyValueList);
        }
        if (!skuList.isEmpty()) {
            productSkuService.saveBatch(skuList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(String productId) {
        this.baseMapper.deleteById(productId);
        productPropertyValueService.remove(new LambdaQueryWrapper<ProductPropertyValue>()
                .eq(ProductPropertyValue::getProductId, productId));
        productSkuService.remove(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId));
    }

    @Override
    public ProductInfoDetailVo getProductDetail(String productId) {
        ProductInfo productInfo = this.getById(productId);
        if (productInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "product not found");
        }

        LambdaQueryWrapper<ProductPropertyValue> propertyValueQuery = new LambdaQueryWrapper<>();
        propertyValueQuery.eq(ProductPropertyValue::getProductId, productId)
                .orderByAsc(ProductPropertyValue::getPropertySort);
        List<ProductPropertyValue> propertyValueList = productPropertyValueService.list(propertyValueQuery);

        List<ProductPropertyVO> productPropertyList = new ArrayList<>();
        Map<String, ProductPropertyVO> propertyVoMap = new HashMap<>();
        for (ProductPropertyValue productPropertyValue : propertyValueList) {
            ProductPropertyVO productPropertyVO = propertyVoMap.get(productPropertyValue.getPropertyId());
            ProductPropertyValueVO propertyValueVo = new ProductPropertyValueVO();
            propertyValueVo.setPropertyValueId(productPropertyValue.getPropertyValueId());
            propertyValueVo.setPropertyCover(productPropertyValue.getPropertyCover());
            propertyValueVo.setPropertyValue(productPropertyValue.getPropertyValue());
            if (productPropertyVO == null) {
                productPropertyVO = new ProductPropertyVO();
                productPropertyVO.setPropertyId(productPropertyValue.getPropertyId());
                productPropertyVO.setPropertyName(productPropertyValue.getPropertyName());
                productPropertyVO.setCoverType(productPropertyValue.getCoverType());
                productPropertyVO.setPropertySort(productPropertyValue.getPropertySort());
                propertyVoMap.put(productPropertyValue.getPropertyId(), productPropertyVO);
                List<ProductPropertyValueVO> productPropertyValueVOS = new ArrayList<>();
                productPropertyValueVOS.add(propertyValueVo);
                productPropertyVO.setPropertyValueList(productPropertyValueVOS);
                productPropertyList.add(productPropertyVO);
            } else {
                productPropertyVO.getPropertyValueList().add(propertyValueVo);
            }
        }

        LambdaQueryWrapper<ProductSku> productSkuQuery = new LambdaQueryWrapper<>();
        productSkuQuery.eq(ProductSku::getProductId, productId)
                .orderByAsc(ProductSku::getSort);
        List<ProductSkuVO> productSkuVOS = new ArrayList<>();
        List<ProductSku> skuList = productSkuService.list(productSkuQuery);
        if (CollectionUtil.isNotEmpty(skuList)) {
            productSkuVOS = BeanUtil.copyToList(skuList, ProductSkuVO.class);
        }

        ProductInfoDetailVo productInfoDetailVO = new ProductInfoDetailVo();
        productInfoDetailVO.setProductInfo(productInfo);
        productInfoDetailVO.setProductPropertyList(productPropertyList);
        productInfoDetailVO.setSkuList(productSkuVOS);
        return productInfoDetailVO;
    }

    @Override
    public ProductInfoDetailVo getVisibleProductDetail(String productId) {
        ProductInfoDetailVo detailVo = getProductDetail(productId);
        ProductInfo productInfo = detailVo.getProductInfo();
        if (productInfo == null || !Objects.equals(productInfo.getStatus(), ProductStatusEnum.ON_SALE.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "product is unavailable");
        }
        return detailVo;
    }

    @Override
    public List<ProductInfoListVO> loadRecommendProducts(Integer limit) {
        int pageSize = normalizeRecommendLimit(limit);
        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setPageNo(1);
        queryDTO.setPageSize(pageSize);
        queryDTO.setCommendType(RECOMMEND_PRODUCT);
        queryDTO.setStatus(ProductStatusEnum.ON_SALE.getStatus());
        log.info("load recommend products, limit={}", pageSize);
        return loadProductList(queryDTO).getRecords();
    }

    private int normalizeRecommendLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_RECOMMEND_LIMIT;
        }
        return Math.min(limit, MAX_RECOMMEND_LIMIT);
    }

    private List<ProductPropertyValue> convertPropertyValues(List<ProductPropertyValueVO> voList) {
        if (voList == null) {
            return List.of();
        }
        return voList.stream().map(vo -> {
            ProductPropertyValue entity = new ProductPropertyValue();
            BeanUtils.copyProperties(vo, entity);
            return entity;
        }).collect(Collectors.toList());
    }

    private List<ProductSku> convertSkus(List<ProductSkuVO> voList) {
        if (voList == null) {
            return List.of();
        }
        return voList.stream().map(vo -> {
            ProductSku entity = new ProductSku();
            BeanUtils.copyProperties(vo, entity);
            return entity;
        }).collect(Collectors.toList());
    }
}
