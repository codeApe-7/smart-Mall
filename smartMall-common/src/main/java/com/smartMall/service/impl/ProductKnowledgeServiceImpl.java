package com.smartMall.service.impl;

import com.smartMall.entities.config.ProductKnowledgeProperties;
import com.smartMall.entities.dto.ProductKnowledgeQueryDTO;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.dto.ReviewQueryDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.entities.vo.ProductKnowledgeVO;
import com.smartMall.entities.vo.ProductPropertyVO;
import com.smartMall.entities.vo.ProductPropertyValueVO;
import com.smartMall.entities.vo.ProductReviewVO;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductKnowledgeService;
import com.smartMall.service.ProductReviewService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Product knowledge service implementation.
 */
@Service
public class ProductKnowledgeServiceImpl implements ProductKnowledgeService {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductReviewService productReviewService;

    @Resource
    private ProductKnowledgeProperties properties;

    @Override
    public PageResultVO<ProductKnowledgeVO> searchKnowledge(ProductKnowledgeQueryDTO dto) {
        ProductKnowledgeQueryDTO safeQuery = dto == null ? new ProductKnowledgeQueryDTO() : dto;
        if (StringTools.isNotEmpty(safeQuery.getProductId())) {
            ProductKnowledgeVO knowledgeVO = getKnowledgeDetail(safeQuery.getProductId());
            return new PageResultVO<>(1, 1, 1L, List.of(knowledgeVO));
        }

        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setPageNo(safeQuery.getPageNo());
        queryDTO.setPageSize(normalizePageSize(safeQuery.getPageSize()));
        queryDTO.setProductName(safeQuery.getKeyword());
        queryDTO.setCategoryIdOrPCategoryId(safeQuery.getCategoryIdOrPCategoryId());
        queryDTO.setSemanticSearch(safeQuery.getSemanticSearch());

        PageResultVO<ProductInfoListVO> productPage = productInfoService.loadVisibleProductList(queryDTO);
        if (productPage.getRecords() == null || productPage.getRecords().isEmpty()) {
            return PageResultVO.empty(queryDTO.getPageNo(), queryDTO.getPageSize());
        }

        List<ProductKnowledgeVO> records = productPage.getRecords().stream()
                .map(ProductInfoListVO::getProductId)
                .map(this::getKnowledgeDetail)
                .toList();
        return new PageResultVO<>(productPage.getPageNo(), productPage.getPageSize(),
                productPage.getTotalCount(), records);
    }

    @Override
    public ProductKnowledgeVO getKnowledgeDetail(String productId) {
        ProductInfoDetailVo detailVo = productInfoService.getVisibleProductDetail(productId);

        ReviewQueryDTO reviewQueryDTO = new ReviewQueryDTO();
        reviewQueryDTO.setProductId(productId);
        reviewQueryDTO.setPageNo(1);
        reviewQueryDTO.setPageSize(normalizeReviewSnippetCount());
        PageResultVO<ProductReviewVO> reviewPage = productReviewService.getProductReviews(reviewQueryDTO);

        ProductKnowledgeVO knowledgeVO = new ProductKnowledgeVO();
        if (detailVo.getProductInfo() != null) {
            knowledgeVO.setProductId(detailVo.getProductInfo().getProductId());
            knowledgeVO.setProductName(detailVo.getProductInfo().getProductName());
            knowledgeVO.setCover(detailVo.getProductInfo().getCover());
            knowledgeVO.setMinPrice(detailVo.getProductInfo().getMinPrice());
            knowledgeVO.setMaxPrice(detailVo.getProductInfo().getMaxPrice());
        }
        knowledgeVO.setSellingPointSummary(buildSellingPointSummary(detailVo));
        knowledgeVO.setReviewSummary(buildReviewSummary(reviewPage));
        knowledgeVO.setAfterSalesSummary(buildAfterSalesSummary());
        knowledgeVO.setReviewCount(reviewPage.getTotalCount().intValue());
        knowledgeVO.setAverageRating(calculateAverageRating(reviewPage.getRecords()));
        knowledgeVO.setKnowledgeTags(buildKnowledgeTags(detailVo, knowledgeVO));
        knowledgeVO.setKnowledgeText(buildKnowledgeText(knowledgeVO));
        return knowledgeVO;
    }

    private int normalizePageSize(Integer pageSize) {
        int safePageSize = pageSize == null || pageSize < 1 ? properties.getDefaultPageSize() : pageSize;
        return Math.max(1, safePageSize);
    }

    private int normalizeReviewSnippetCount() {
        Integer maxReviewSnippetCount = properties.getMaxReviewSnippetCount();
        return maxReviewSnippetCount == null || maxReviewSnippetCount < 1 ? 3 : maxReviewSnippetCount;
    }

    private String buildSellingPointSummary(ProductInfoDetailVo detailVo) {
        List<String> fragments = new ArrayList<>();
        if (detailVo.getProductInfo() != null && StringTools.isNotEmpty(detailVo.getProductInfo().getProductDesc())) {
            fragments.add("商品描述：" + detailVo.getProductInfo().getProductDesc().trim());
        }
        int maxPropertyCount = properties.getMaxPropertySnippetCount() == null ? 4 : properties.getMaxPropertySnippetCount();
        if (detailVo.getProductPropertyList() != null) {
            detailVo.getProductPropertyList().stream()
                    .limit(Math.max(maxPropertyCount, 1))
                    .forEach(property -> fragments.add(buildPropertySnippet(property)));
        }
        return String.join("；", fragments);
    }

    private String buildPropertySnippet(ProductPropertyVO property) {
        if (property.getPropertyValueList() == null || property.getPropertyValueList().isEmpty()) {
            return property.getPropertyName();
        }
        String joinedValues = property.getPropertyValueList().stream()
                .map(ProductPropertyValueVO::getPropertyValue)
                .filter(StringTools::isNotEmpty)
                .limit(3)
                .reduce((left, right) -> left + "、" + right)
                .orElse("");
        return property.getPropertyName() + "：" + joinedValues;
    }

    private String buildReviewSummary(PageResultVO<ProductReviewVO> reviewPage) {
        if (reviewPage.getRecords() == null || reviewPage.getRecords().isEmpty()) {
            return "暂无用户评价，可结合商品描述和规格信息进行判断。";
        }
        double averageRating = calculateAverageRating(reviewPage.getRecords());
        String highlights = reviewPage.getRecords().stream()
                .map(ProductReviewVO::getContent)
                .filter(StringTools::isNotEmpty)
                .limit(normalizeReviewSnippetCount())
                .reduce((left, right) -> left + "；" + right)
                .orElse("暂无有效评价内容。");
        return String.format(Locale.ROOT, "当前收录 %d 条评价，示例评分均值 %.1f。用户反馈：%s",
                reviewPage.getTotalCount(), averageRating, highlights);
    }

    private String buildAfterSalesSummary() {
        List<String> afterSalesHighlights = properties.getAfterSalesHighlights();
        if (afterSalesHighlights == null || afterSalesHighlights.isEmpty()) {
            return "支持基础售后咨询与订单跟踪服务。";
        }
        return String.join("；", afterSalesHighlights);
    }

    private Double calculateAverageRating(List<ProductReviewVO> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0D;
        }
        return reviews.stream()
                .map(ProductReviewVO::getRating)
                .filter(rating -> rating != null && rating > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0D);
    }

    private List<String> buildKnowledgeTags(ProductInfoDetailVo detailVo, ProductKnowledgeVO knowledgeVO) {
        List<String> tags = new ArrayList<>();
        if (detailVo.getProductInfo() != null && StringTools.isNotEmpty(detailVo.getProductInfo().getProductName())) {
            tags.add(detailVo.getProductInfo().getProductName());
        }
        if (detailVo.getProductPropertyList() != null) {
            detailVo.getProductPropertyList().stream()
                    .map(ProductPropertyVO::getPropertyName)
                    .filter(StringTools::isNotEmpty)
                    .limit(3)
                    .forEach(tags::add);
        }
        if (knowledgeVO.getAverageRating() != null && knowledgeVO.getAverageRating() > 0) {
            tags.add(String.format(Locale.ROOT, "%.1f分口碑", knowledgeVO.getAverageRating()));
        }
        tags.add("售后");
        return tags.stream().distinct().toList();
    }

    private String buildKnowledgeText(ProductKnowledgeVO knowledgeVO) {
        return String.join("\n",
                "商品：" + defaultText(knowledgeVO.getProductName()),
                "卖点：" + defaultText(knowledgeVO.getSellingPointSummary()),
                "评价摘要：" + defaultText(knowledgeVO.getReviewSummary()),
                "售后说明：" + defaultText(knowledgeVO.getAfterSalesSummary()));
    }

    private String defaultText(String value) {
        return StringTools.isEmpty(value) ? "暂无" : value;
    }
}
