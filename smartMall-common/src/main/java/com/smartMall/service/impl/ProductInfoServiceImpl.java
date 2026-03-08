package com.smartMall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.config.ProductSearchProperties;
import com.smartMall.entities.config.UserPreferenceProperties;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.ProductPropertyValue;
import com.smartMall.entities.domain.ProductSku;
import com.smartMall.entities.domain.SysCategory;
import com.smartMall.entities.domain.UserPreference;
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
import com.smartMall.service.UserPreferenceService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    @Resource
    private ProductSearchProperties productSearchProperties = new ProductSearchProperties();

    @Resource
    private UserPreferenceProperties userPreferenceProperties = new UserPreferenceProperties();

    @Resource
    private ProductPropertyValueService productPropertyValueService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private SysCategoryService sysCategoryService;

    @Lazy
    @Resource
    private UserPreferenceService userPreferenceService;

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

        List<ProductInfoListVO> voList = buildProductListVOs(productList);
        return new PageResultVO<>(safeQuery.getPageNo(), safeQuery.getPageSize(), page.getTotal(), voList);
    }

    @Override
    public PageResultVO<ProductInfoListVO> loadVisibleProductList(ProductQueryDTO queryDTO) {
        ProductQueryDTO visibleQuery = queryDTO == null ? new ProductQueryDTO() : queryDTO;
        visibleQuery.setStatus(ProductStatusEnum.ON_SALE.getStatus());
        log.info("load visible products, pageNo={}, pageSize={}, categoryId={}, commendType={}",
                visibleQuery.getPageNo(), visibleQuery.getPageSize(),
                visibleQuery.getCategoryIdOrPCategoryId(), visibleQuery.getCommendType());
        if (shouldUseSemanticSearch(visibleQuery)) {
            PageResultVO<ProductInfoListVO> semanticSearchResult = loadVisibleProductListBySemanticSearch(visibleQuery);
            if (semanticSearchResult != null) {
                return semanticSearchResult;
            }
        }
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

    @Override
    public List<ProductInfoListVO> loadPersonalizedRecommendProducts(String userId, Integer limit) {
        int pageSize = normalizeRecommendLimit(limit);
        if (StringTools.isEmpty(userId)) {
            return loadRecommendProducts(pageSize);
        }
        try {
            UserPreference preference = getUserPreference(userId);
            if (preference == null) {
                userPreferenceService.refreshUserPreference(userId);
                preference = getUserPreference(userId);
            }
            if (preference == null || StringTools.isEmpty(preference.getFavoriteCategoryIds())) {
                log.info("no user preference found, fallback to general recommend, userId={}", userId);
                return loadRecommendProducts(pageSize);
            }

            List<String> categoryIds = Arrays.stream(preference.getFavoriteCategoryIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            List<String> recentProductIds = StringTools.isEmpty(preference.getRecentProductIds())
                    ? List.of()
                    : Arrays.stream(preference.getRecentProductIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            LambdaQueryWrapper<ProductInfo> queryWrapper = new LambdaQueryWrapper<ProductInfo>()
                    .eq(ProductInfo::getStatus, ProductStatusEnum.ON_SALE.getStatus())
                    .in(ProductInfo::getCategoryId, categoryIds);
            if (preference.getMinPricePreference() != null) {
                queryWrapper.ge(ProductInfo::getMinPrice, preference.getMinPricePreference());
            }
            if (preference.getMaxPricePreference() != null) {
                queryWrapper.le(ProductInfo::getMinPrice, preference.getMaxPricePreference());
            }
            if (!recentProductIds.isEmpty()) {
                queryWrapper.notIn(ProductInfo::getProductId, recentProductIds);
            }
            queryWrapper.orderByDesc(ProductInfo::getTotalSale);

            Page<ProductInfo> page = new Page<>(1, pageSize);
            this.page(page, queryWrapper);
            List<ProductInfoListVO> personalized = new ArrayList<>(buildProductListVOs(page.getRecords()));

            if (personalized.size() < pageSize) {
                List<String> existingIds = personalized.stream()
                        .map(ProductInfoListVO::getProductId)
                        .collect(Collectors.toList());
                existingIds.addAll(recentProductIds);
                List<ProductInfoListVO> general = loadRecommendProducts(pageSize);
                for (ProductInfoListVO item : general) {
                    if (!existingIds.contains(item.getProductId()) && personalized.size() < pageSize) {
                        personalized.add(item);
                        existingIds.add(item.getProductId());
                    }
                }
            }
            log.info("load personalized recommend products, userId={}, count={}", userId, personalized.size());
            return personalized;
        } catch (Exception e) {
            log.warn("personalized recommend failed, fallback to general, userId={}", userId, e);
            return loadRecommendProducts(pageSize);
        }
    }

    private int normalizeRecommendLimit(Integer limit) {
        int defaultLimit = userPreferenceProperties.getDefaultRecommendLimit() > 0
                ? userPreferenceProperties.getDefaultRecommendLimit()
                : DEFAULT_RECOMMEND_LIMIT;
        if (limit == null || limit < 1) {
            return defaultLimit;
        }
        return Math.min(limit, MAX_RECOMMEND_LIMIT);
    }

    private UserPreference getUserPreference(String userId) {
        return userPreferenceService.getOne(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, userId)
                .last("LIMIT 1"));
    }

    private boolean shouldUseSemanticSearch(ProductQueryDTO queryDTO) {
        return productSearchProperties.isSemanticEnabled()
                && !Boolean.FALSE.equals(queryDTO.getSemanticSearch())
                && StringTools.isNotEmpty(queryDTO.getProductName());
    }

    private PageResultVO<ProductInfoListVO> loadVisibleProductListBySemanticSearch(ProductQueryDTO queryDTO) {
        try {
            JSONObject requestJson = buildSemanticSearchRequest(queryDTO);
            Request request = new Request.Builder()
                    .url(buildElasticsearchSearchUrl())
                    .post(RequestBody.create(requestJson.toJSONString(), JSON_MEDIA_TYPE))
                    .build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.warn("semantic search fallback to db, httpStatus={}", response.code());
                    return null;
                }
                JSONObject responseJson = JSONObject.parseObject(response.body().string());
                JSONObject hitsJson = responseJson.getJSONObject("hits");
                if (hitsJson == null) {
                    return null;
                }
                long totalCount = extractSemanticSearchTotal(hitsJson);
                List<String> productIds = extractSemanticProductIds(hitsJson);
                if (productIds.isEmpty()) {
                    log.info("semantic search returned empty result, fallback to db, keyword={}", queryDTO.getProductName());
                    return null;
                }
                List<ProductInfoListVO> records = loadVisibleProductListByIds(productIds);
                if (records.isEmpty()) {
                    log.info("semantic search ids not found in db, fallback to db, keyword={}", queryDTO.getProductName());
                    return null;
                }
                log.info("semantic search success, keyword={}, totalCount={}, recordCount={}",
                        queryDTO.getProductName(), totalCount, records.size());
                return new PageResultVO<>(queryDTO.getPageNo(), queryDTO.getPageSize(), totalCount, records);
            }
        } catch (Exception e) {
            log.warn("semantic search fallback to db, keyword={}", queryDTO.getProductName(), e);
            return null;
        }
    }

    private JSONObject buildSemanticSearchRequest(ProductQueryDTO queryDTO) {
        JSONObject root = new JSONObject();
        root.put("from", queryDTO.getOffset());
        root.put("size", normalizeSemanticCandidateSize(queryDTO.getPageSize()));
        root.put("track_total_hits", true);

        JSONArray sourceFields = new JSONArray();
        sourceFields.add("productId");
        root.put("_source", sourceFields);

        JSONObject boolQuery = new JSONObject();
        JSONArray mustArray = new JSONArray();
        JSONArray filterArray = new JSONArray();

        JSONObject multiMatch = new JSONObject();
        multiMatch.put("query", queryDTO.getProductName());
        JSONArray fieldsArray = new JSONArray();
        fieldsArray.add("productName^4");
        fieldsArray.add("productDesc^2");
        fieldsArray.add("categoryName");
        multiMatch.put("fields", fieldsArray);
        multiMatch.put("minimum_should_match", "60%");
        mustArray.add(JSONObject.of("multi_match", multiMatch));

        filterArray.add(JSONObject.of("term", JSONObject.of("status", ProductStatusEnum.ON_SALE.getStatus())));
        if (queryDTO.getCommendType() != null) {
            filterArray.add(JSONObject.of("term", JSONObject.of("commendType", queryDTO.getCommendType())));
        }
        if (StringTools.isNotEmpty(queryDTO.getCategoryIdOrPCategoryId())) {
            JSONObject categoryBool = new JSONObject();
            JSONArray shouldArray = new JSONArray();
            shouldArray.add(JSONObject.of("term", JSONObject.of("categoryId", queryDTO.getCategoryIdOrPCategoryId())));
            shouldArray.add(JSONObject.of("term", JSONObject.of("pCategoryId", queryDTO.getCategoryIdOrPCategoryId())));
            categoryBool.put("should", shouldArray);
            categoryBool.put("minimum_should_match", 1);
            filterArray.add(JSONObject.of("bool", categoryBool));
        }

        boolQuery.put("must", mustArray);
        boolQuery.put("filter", filterArray);
        root.put("query", JSONObject.of("bool", boolQuery));
        return root;
    }

    private String buildElasticsearchSearchUrl() {
        String uri = productSearchProperties.getElasticsearchUri();
        if (StringTools.isEmpty(uri)) {
            uri = "http://127.0.0.1:9200";
        }
        String normalizedUri = uri.split(",")[0].trim();
        if (normalizedUri.endsWith("/")) {
            normalizedUri = normalizedUri.substring(0, normalizedUri.length() - 1);
        }
        return normalizedUri + "/" + productSearchProperties.getProductIndexName() + "/_search";
    }

    private int normalizeSemanticCandidateSize(Integer pageSize) {
        int basePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int candidateSize = productSearchProperties.getSemanticCandidateSize() == null
                ? basePageSize : productSearchProperties.getSemanticCandidateSize();
        return Math.max(basePageSize, candidateSize);
    }

    private long extractSemanticSearchTotal(JSONObject hitsJson) {
        JSONObject totalJson = hitsJson.getJSONObject("total");
        if (totalJson == null) {
            return 0L;
        }
        return totalJson.getLongValue("value");
    }

    private List<String> extractSemanticProductIds(JSONObject hitsJson) {
        JSONArray hitArray = hitsJson.getJSONArray("hits");
        if (hitArray == null || hitArray.isEmpty()) {
            return List.of();
        }
        List<String> productIds = new ArrayList<>();
        for (int i = 0; i < hitArray.size(); i++) {
            JSONObject hitJson = hitArray.getJSONObject(i);
            JSONObject sourceJson = hitJson.getJSONObject("_source");
            String productId = sourceJson == null ? null : sourceJson.getString("productId");
            if (StringTools.isEmpty(productId)) {
                productId = hitJson.getString("_id");
            }
            if (StringTools.isNotEmpty(productId)) {
                productIds.add(productId);
            }
        }
        return productIds.stream().distinct().toList();
    }

    private List<ProductInfoListVO> loadVisibleProductListByIds(List<String> productIds) {
        List<ProductInfo> productList = this.list(new LambdaQueryWrapper<ProductInfo>()
                .in(ProductInfo::getProductId, productIds)
                .eq(ProductInfo::getStatus, ProductStatusEnum.ON_SALE.getStatus()));
        if (productList.isEmpty()) {
            return List.of();
        }
        Map<String, ProductInfo> productMap = productList.stream().collect(Collectors.toMap(
                ProductInfo::getProductId,
                product -> product,
                (left, right) -> left));
        List<ProductInfo> orderedProducts = productIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .toList();
        return buildProductListVOs(orderedProducts);
    }

    private List<ProductInfoListVO> buildProductListVOs(List<ProductInfo> productList) {
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
        List<ProductSku> allSkus = productIds.isEmpty() ? List.of() : productSkuService.list(
                new LambdaQueryWrapper<ProductSku>().in(ProductSku::getProductId, productIds));
        Map<String, List<ProductSku>> skuGroupMap = allSkus.stream()
                .collect(Collectors.groupingBy(ProductSku::getProductId));

        Map<String, String> finalCategoryNameMap = categoryNameMap;
        return productList.stream().map(product -> {
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
