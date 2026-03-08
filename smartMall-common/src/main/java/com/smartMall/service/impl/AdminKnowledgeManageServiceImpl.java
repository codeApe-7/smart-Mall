package com.smartMall.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.entities.config.ProductKnowledgeProperties;
import com.smartMall.entities.config.ProductSearchProperties;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.SysCategory;
import com.smartMall.entities.enums.ProductStatusEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.AdminKnowledgeIndexSummaryVO;
import com.smartMall.entities.vo.AdminKnowledgeIndexSyncResultVO;
import com.smartMall.entities.vo.ProductKnowledgeVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.service.AdminKnowledgeManageService;
import com.smartMall.service.AiConfigService;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductKnowledgeService;
import com.smartMall.service.SysCategoryService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Admin knowledge manage service implementation.
 */
@Service
@Slf4j
public class AdminKnowledgeManageServiceImpl implements AdminKnowledgeManageService {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final MediaType NDJSON_MEDIA_TYPE = MediaType.get("application/x-ndjson; charset=utf-8");

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductKnowledgeService productKnowledgeService;

    @Resource
    private SysCategoryService sysCategoryService;

    @Resource
    private AiConfigService aiConfigService;

    @Override
    public ProductKnowledgeVO getProductKnowledge(String productId) {
        return productKnowledgeService.getKnowledgeDetail(productId);
    }

    @Override
    public AdminKnowledgeIndexSummaryVO getIndexSummary() {
        ProductSearchProperties searchProperties = aiConfigService.getProductSearchConfig();
        ProductKnowledgeProperties knowledgeProperties = aiConfigService.getProductKnowledgeConfig();

        AdminKnowledgeIndexSummaryVO summaryVO = new AdminKnowledgeIndexSummaryVO();
        summaryVO.setSemanticSearchEnabled(searchProperties.isSemanticEnabled());
        summaryVO.setProductKnowledgeEnabled(knowledgeProperties.isEnabled());
        summaryVO.setElasticsearchUri(searchProperties.getElasticsearchUri());
        summaryVO.setProductIndexName(searchProperties.getProductIndexName());
        summaryVO.setOnSaleProductCount(countOnSaleProducts());
        summaryVO.setCheckedTime(new Date());

        try {
            long indexedDocumentCount = countIndexDocuments(searchProperties);
            summaryVO.setReachable(Boolean.TRUE);
            summaryVO.setIndexedDocumentCount(indexedDocumentCount);
        } catch (Exception e) {
            log.warn("load knowledge index summary failed", e);
            summaryVO.setReachable(Boolean.FALSE);
            summaryVO.setIndexedDocumentCount(0L);
        }
        return summaryVO;
    }

    @Override
    public AdminKnowledgeIndexSyncResultVO syncProduct(String productId) {
        long startTime = System.currentTimeMillis();
        ProductInfo productInfo = getProductInfo(productId);
        ProductSearchProperties searchProperties = aiConfigService.getProductSearchConfig();
        String indexName = searchProperties.getProductIndexName();
        ensureIndexExists(searchProperties);

        List<String> failedProductIds = new ArrayList<>();
        int successCount = 0;
        try {
            if (!Objects.equals(productInfo.getStatus(), ProductStatusEnum.ON_SALE.getStatus())) {
                deleteDocument(searchProperties, productId);
            } else {
                JSONObject document = buildIndexDocument(productInfo, loadCategoryNameMap(List.of(productInfo)));
                indexDocument(searchProperties, productId, document);
            }
            successCount = 1;
        } catch (Exception e) {
            log.warn("sync product knowledge index failed, productId={}", productId, e);
            failedProductIds.add(productId);
        }

        return buildSyncResult("single_sync", indexName, 1, successCount, failedProductIds, startTime);
    }

    @Override
    public AdminKnowledgeIndexSyncResultVO rebuildIndex() {
        long startTime = System.currentTimeMillis();
        ProductSearchProperties searchProperties = aiConfigService.getProductSearchConfig();
        String indexName = searchProperties.getProductIndexName();
        List<ProductInfo> onSaleProducts = productInfoService.list(new LambdaQueryWrapper<ProductInfo>()
                .eq(ProductInfo::getStatus, ProductStatusEnum.ON_SALE.getStatus())
                .orderByDesc(ProductInfo::getCreateTime));
        List<String> failedProductIds = new ArrayList<>();

        recreateIndex(searchProperties);
        Map<String, String> categoryNameMap = loadCategoryNameMap(onSaleProducts);
        Map<String, JSONObject> documentMap = new LinkedHashMap<>();
        for (ProductInfo productInfo : onSaleProducts) {
            try {
                documentMap.put(productInfo.getProductId(), buildIndexDocument(productInfo, categoryNameMap));
            } catch (Exception e) {
                log.warn("build knowledge index document failed, productId={}", productInfo.getProductId(), e);
                failedProductIds.add(productInfo.getProductId());
            }
        }
        failedProductIds.addAll(bulkIndexDocuments(searchProperties, documentMap));
        int successCount = Math.max(0, onSaleProducts.size() - failedProductIds.size());
        return buildSyncResult("full_rebuild", indexName, onSaleProducts.size(), successCount, failedProductIds, startTime);
    }

    private AdminKnowledgeIndexSyncResultVO buildSyncResult(String operationType,
                                                            String indexName,
                                                            int requestedCount,
                                                            int successCount,
                                                            List<String> failedProductIds,
                                                            long startTime) {
        AdminKnowledgeIndexSyncResultVO resultVO = new AdminKnowledgeIndexSyncResultVO();
        resultVO.setOperationType(operationType);
        resultVO.setProductIndexName(indexName);
        resultVO.setRequestedCount(requestedCount);
        resultVO.setSuccessCount(successCount);
        resultVO.setFailCount(failedProductIds.size());
        resultVO.setFailedProductIds(failedProductIds.stream().distinct().toList());
        resultVO.setDurationMs(System.currentTimeMillis() - startTime);
        resultVO.setCompletedTime(new Date());
        return resultVO;
    }

    private ProductInfo getProductInfo(String productId) {
        if (StringTools.isEmpty(productId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "productId is required");
        }
        ProductInfo productInfo = productInfoService.getById(productId);
        if (productInfo == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "product not found");
        }
        return productInfo;
    }

    private long countOnSaleProducts() {
        return productInfoService.count(new LambdaQueryWrapper<ProductInfo>()
                .eq(ProductInfo::getStatus, ProductStatusEnum.ON_SALE.getStatus()));
    }

    private Map<String, String> loadCategoryNameMap(List<ProductInfo> productList) {
        if (productList == null || productList.isEmpty()) {
            return Map.of();
        }
        List<String> categoryIds = productList.stream()
                .flatMap(item -> Stream.of(item.getCategoryId(), item.getPCategoryId()))
                .filter(StringTools::isNotEmpty)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return sysCategoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(SysCategory::getCategoryId, SysCategory::getCategoryName, (left, right) -> left));
    }

    private JSONObject buildIndexDocument(ProductInfo productInfo, Map<String, String> categoryNameMap) {
        ProductKnowledgeVO knowledgeVO = productKnowledgeService.getKnowledgeDetail(productInfo.getProductId());
        JSONObject document = new JSONObject();
        document.put("productId", productInfo.getProductId());
        document.put("productName", productInfo.getProductName());
        document.put("productDesc", productInfo.getProductDesc());
        document.put("cover", productInfo.getCover());
        document.put("categoryId", productInfo.getCategoryId());
        document.put("pCategoryId", productInfo.getPCategoryId());
        document.put("categoryName", categoryNameMap.getOrDefault(productInfo.getCategoryId(), ""));
        document.put("pCategoryName", categoryNameMap.getOrDefault(productInfo.getPCategoryId(), ""));
        document.put("status", productInfo.getStatus());
        document.put("commendType", productInfo.getCommendType());
        document.put("minPrice", productInfo.getMinPrice());
        document.put("maxPrice", productInfo.getMaxPrice());
        document.put("totalSale", productInfo.getTotalSale());
        document.put("knowledgeText", knowledgeVO.getKnowledgeText());
        document.put("sellingPointSummary", knowledgeVO.getSellingPointSummary());
        document.put("reviewSummary", knowledgeVO.getReviewSummary());
        document.put("afterSalesSummary", knowledgeVO.getAfterSalesSummary());
        document.put("reviewCount", knowledgeVO.getReviewCount());
        document.put("averageRating", knowledgeVO.getAverageRating());
        document.put("knowledgeTags", knowledgeVO.getKnowledgeTags());
        document.put("syncTime", new Date().getTime());
        return document;
    }

    private long countIndexDocuments(ProductSearchProperties searchProperties) throws IOException {
        Request request = new Request.Builder()
                .url(buildIndexUrl(searchProperties) + "/_count")
                .get()
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() == 404) {
                return 0L;
            }
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("count index failed, httpStatus=" + response.code());
            }
            JSONObject responseJson = JSONObject.parseObject(response.body().string());
            return responseJson.getLongValue("count");
        }
    }

    private void ensureIndexExists(ProductSearchProperties searchProperties) {
        try {
            if (indexExists(searchProperties)) {
                return;
            }
            createIndex(searchProperties);
        } catch (IOException e) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "search index is unavailable");
        }
    }

    private void recreateIndex(ProductSearchProperties searchProperties) {
        try {
            if (indexExists(searchProperties)) {
                deleteIndex(searchProperties);
            }
            createIndex(searchProperties);
        } catch (IOException e) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "rebuild search index failed");
        }
    }

    private boolean indexExists(ProductSearchProperties searchProperties) throws IOException {
        Request request = new Request.Builder()
                .url(buildIndexUrl(searchProperties))
                .head()
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() == 404) {
                return false;
            }
            if (!response.isSuccessful()) {
                throw new IOException("check index exists failed, httpStatus=" + response.code());
            }
            return true;
        }
    }

    private void createIndex(ProductSearchProperties searchProperties) throws IOException {
        Request request = new Request.Builder()
                .url(buildIndexUrl(searchProperties))
                .put(RequestBody.create("{}", JSON_MEDIA_TYPE))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("create index failed, httpStatus=" + response.code());
            }
        }
    }

    private void deleteIndex(ProductSearchProperties searchProperties) throws IOException {
        Request request = new Request.Builder()
                .url(buildIndexUrl(searchProperties))
                .delete()
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() == 404) {
                return;
            }
            if (!response.isSuccessful()) {
                throw new IOException("delete index failed, httpStatus=" + response.code());
            }
        }
    }

    private void deleteDocument(ProductSearchProperties searchProperties, String productId) throws IOException {
        Request request = new Request.Builder()
                .url(buildIndexUrl(searchProperties) + "/_doc/" + productId)
                .delete()
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() == 404) {
                return;
            }
            if (!response.isSuccessful()) {
                throw new IOException("delete document failed, httpStatus=" + response.code());
            }
        }
    }

    private void indexDocument(ProductSearchProperties searchProperties, String productId, JSONObject document)
            throws IOException {
        Request request = new Request.Builder()
                .url(buildIndexUrl(searchProperties) + "/_doc/" + productId + "?refresh=true")
                .put(RequestBody.create(document.toJSONString(), JSON_MEDIA_TYPE))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("index document failed, httpStatus=" + response.code());
            }
        }
    }

    private List<String> bulkIndexDocuments(ProductSearchProperties searchProperties, Map<String, JSONObject> documentMap) {
        if (documentMap.isEmpty()) {
            return List.of();
        }
        StringBuilder bodyBuilder = new StringBuilder();
        documentMap.forEach((productId, document) -> {
            bodyBuilder.append(JSON.toJSONString(Map.of("index", Map.of("_id", productId)))).append('\n');
            bodyBuilder.append(document.toJSONString()).append('\n');
        });

        Request request = new Request.Builder()
                .url(buildIndexUrl(searchProperties) + "/_bulk?refresh=true")
                .post(RequestBody.create(bodyBuilder.toString(), NDJSON_MEDIA_TYPE))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "bulk rebuild search index failed");
            }
            JSONObject responseJson = JSONObject.parseObject(response.body().string());
            if (!responseJson.getBooleanValue("errors")) {
                return List.of();
            }
            JSONArray items = responseJson.getJSONArray("items");
            if (items == null || items.isEmpty()) {
                return new ArrayList<>(documentMap.keySet());
            }
            List<String> failedProductIds = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject indexResult = item == null ? null : item.getJSONObject("index");
                if (indexResult != null && indexResult.containsKey("error")) {
                    failedProductIds.add(indexResult.getString("_id"));
                }
            }
            return failedProductIds;
        } catch (IOException e) {
            throw new BusinessException(ResponseCodeEnum.OPERATION_FAILED, "bulk rebuild search index failed");
        }
    }

    private String buildIndexUrl(ProductSearchProperties searchProperties) {
        String uri = searchProperties.getElasticsearchUri();
        if (StringTools.isEmpty(uri)) {
            uri = "http://127.0.0.1:9200";
        }
        String normalizedUri = uri.split(",")[0].trim();
        if (normalizedUri.endsWith("/")) {
            normalizedUri = normalizedUri.substring(0, normalizedUri.length() - 1);
        }
        return normalizedUri + "/" + searchProperties.getProductIndexName();
    }
}
