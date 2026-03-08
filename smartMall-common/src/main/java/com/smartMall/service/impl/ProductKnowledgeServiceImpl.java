package com.smartMall.service.impl;

import com.smartMall.entities.config.ProductKnowledgeProperties;
import com.smartMall.entities.dto.ProductKnowledgeCompareDTO;
import com.smartMall.entities.dto.ProductKnowledgeQueryDTO;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.dto.ReviewQueryDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductKnowledgeCompareCellVO;
import com.smartMall.entities.vo.ProductKnowledgeCompareDimensionVO;
import com.smartMall.entities.vo.ProductKnowledgeCompareVO;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.entities.vo.ProductKnowledgeVO;
import com.smartMall.entities.vo.ProductPropertyVO;
import com.smartMall.entities.vo.ProductPropertyValueVO;
import com.smartMall.entities.vo.ProductReviewVO;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductKnowledgeService;
import com.smartMall.service.ProductReviewService;
import com.smartMall.service.AiConfigService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private AiConfigService aiConfigService;

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

    @Override
    public ProductKnowledgeCompareVO compareKnowledge(ProductKnowledgeCompareDTO dto) {
        ProductKnowledgeCompareDTO safeQuery = dto == null ? new ProductKnowledgeCompareDTO() : dto;
        List<ProductKnowledgeVO> products = loadCompareProducts(safeQuery);

        ProductKnowledgeCompareVO compareVO = new ProductKnowledgeCompareVO();
        compareVO.setComparable(products.size() >= 2);
        compareVO.setProducts(products);
        compareVO.setDimensions(buildCompareDimensions(products));
        compareVO.setCompareSummary(buildCompareSummary(products));
        compareVO.setDecisionSuggestions(buildDecisionSuggestions(products));
        compareVO.setComparisonText(buildComparisonText(compareVO));
        return compareVO;
    }

    private int normalizePageSize(Integer pageSize) {
        ProductKnowledgeProperties properties = aiConfigService.getProductKnowledgeConfig();
        int safePageSize = pageSize == null || pageSize < 1 ? properties.getDefaultPageSize() : pageSize;
        return Math.max(1, safePageSize);
    }

    private int normalizeReviewSnippetCount() {
        ProductKnowledgeProperties properties = aiConfigService.getProductKnowledgeConfig();
        Integer maxReviewSnippetCount = properties.getMaxReviewSnippetCount();
        return maxReviewSnippetCount == null || maxReviewSnippetCount < 1 ? 3 : maxReviewSnippetCount;
    }

    private int normalizeCompareCount(Integer maxCount) {
        ProductKnowledgeProperties properties = aiConfigService.getProductKnowledgeConfig();
        int safeMaxCount = maxCount == null || maxCount < 1 ? properties.getMaxCompareCount() : maxCount;
        return Math.max(2, safeMaxCount);
    }

    private List<ProductKnowledgeVO> loadCompareProducts(ProductKnowledgeCompareDTO dto) {
        int maxCount = normalizeCompareCount(dto.getMaxCount());
        if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {
            return dto.getProductIds().stream()
                    .filter(StringTools::isNotEmpty)
                    .map(String::trim)
                    .filter(StringTools::isNotEmpty)
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .limit(maxCount)
                    .map(this::getKnowledgeDetail)
                    .toList();
        }
        if (StringTools.isEmpty(dto.getKeyword())) {
            return List.of();
        }
        ProductKnowledgeQueryDTO queryDTO = new ProductKnowledgeQueryDTO();
        queryDTO.setKeyword(dto.getKeyword());
        queryDTO.setPageNo(1);
        queryDTO.setPageSize(maxCount);
        queryDTO.setSemanticSearch(dto.getSemanticSearch());
        PageResultVO<ProductKnowledgeVO> knowledgePage = searchKnowledge(queryDTO);
        if (knowledgePage.getRecords() == null) {
            return List.of();
        }
        return knowledgePage.getRecords().stream()
                .limit(maxCount)
                .toList();
    }

    private List<ProductKnowledgeCompareDimensionVO> buildCompareDimensions(List<ProductKnowledgeVO> products) {
        if (products.isEmpty()) {
            return List.of();
        }
        Set<String> cheapestProductIds = resolveCheapestProductIds(products);
        Set<String> topRatedProductIds = resolveTopRatedProductIds(products);
        return List.of(
                buildDimension("price", "价格区间", products, this::formatPriceRange, cheapestProductIds),
                buildDimension("selling-point", "核心卖点", products,
                        product -> abbreviate(product.getSellingPointSummary(), 80), Set.of()),
                buildDimension("review", "口碑评价", products, this::formatReviewDimension, topRatedProductIds),
                buildDimension("after-sales", "售后说明", products,
                        product -> abbreviate(product.getAfterSalesSummary(), 80), Set.of()),
                buildDimension("tags", "知识标签", products, this::formatKnowledgeTags, Set.of())
        );
    }

    private ProductKnowledgeCompareDimensionVO buildDimension(String dimensionKey,
                                                             String dimensionName,
                                                             List<ProductKnowledgeVO> products,
                                                             Function<ProductKnowledgeVO, String> valueResolver,
                                                             Set<String> highlightProductIds) {
        ProductKnowledgeCompareDimensionVO dimensionVO = new ProductKnowledgeCompareDimensionVO();
        dimensionVO.setDimensionKey(dimensionKey);
        dimensionVO.setDimensionName(dimensionName);
        dimensionVO.setValues(products.stream()
                .map(product -> buildCompareCell(product, valueResolver.apply(product), highlightProductIds))
                .toList());
        return dimensionVO;
    }

    private ProductKnowledgeCompareCellVO buildCompareCell(ProductKnowledgeVO product,
                                                           String value,
                                                           Set<String> highlightProductIds) {
        ProductKnowledgeCompareCellVO cellVO = new ProductKnowledgeCompareCellVO();
        cellVO.setProductId(product.getProductId());
        cellVO.setProductName(product.getProductName());
        cellVO.setValue(defaultText(value));
        cellVO.setHighlight(highlightProductIds.contains(product.getProductId()));
        return cellVO;
    }

    private Set<String> resolveCheapestProductIds(List<ProductKnowledgeVO> products) {
        return resolveMatchedProductIds(products, ProductKnowledgeVO::getMinPrice, Comparator.naturalOrder());
    }

    private Set<String> resolveTopRatedProductIds(List<ProductKnowledgeVO> products) {
        return resolveMatchedProductIds(products, ProductKnowledgeVO::getAverageRating, Comparator.reverseOrder());
    }

    private <T> Set<String> resolveMatchedProductIds(List<ProductKnowledgeVO> products,
                                                     Function<ProductKnowledgeVO, T> extractor,
                                                     Comparator<T> comparator) {
        return products.stream()
                .map(extractor)
                .filter(value -> value != null)
                .min(comparator)
                .map(target -> products.stream()
                        .filter(product -> {
                            T value = extractor.apply(product);
                            return value != null && comparator.compare(value, target) == 0;
                        })
                        .map(ProductKnowledgeVO::getProductId)
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .orElseGet(LinkedHashSet::new);
    }

    private String buildCompareSummary(List<ProductKnowledgeVO> products) {
        if (products.isEmpty()) {
            return "当前没有匹配到可对比的商品知识卡片。";
        }
        if (products.size() == 1) {
            return String.format(Locale.ROOT, "当前仅匹配到 1 款商品：%s，建议补充更明确的商品名称或使用多个商品ID继续对比。",
                    defaultText(products.getFirst().getProductName()));
        }
        ProductKnowledgeVO cheapestProduct = findCheapestProduct(products);
        ProductKnowledgeVO topRatedProduct = findTopRatedProduct(products);
        StringBuilder summary = new StringBuilder();
        summary.append(String.format(Locale.ROOT, "已生成 %d 款商品的结构化对比，可重点关注价格、卖点、口碑和售后差异。",
                products.size()));
        if (cheapestProduct != null) {
            summary.append("预算优先可先看 ")
                    .append(defaultText(cheapestProduct.getProductName()))
                    .append("。");
        }
        if (topRatedProduct != null) {
            summary.append("口碑更突出的商品是 ")
                    .append(defaultText(topRatedProduct.getProductName()))
                    .append("。");
        }
        return summary.toString();
    }

    private List<String> buildDecisionSuggestions(List<ProductKnowledgeVO> products) {
        if (products.isEmpty()) {
            return List.of("建议补充更明确的商品关键词后再发起比较。");
        }
        List<String> suggestions = new ArrayList<>();
        ProductKnowledgeVO cheapestProduct = findCheapestProduct(products);
        if (cheapestProduct != null) {
            suggestions.add("预算优先可重点关注 " + defaultText(cheapestProduct.getProductName()));
        }
        ProductKnowledgeVO topRatedProduct = findTopRatedProduct(products);
        if (topRatedProduct != null) {
            suggestions.add("口碑优先可重点关注 " + defaultText(topRatedProduct.getProductName()));
        }
        suggestions.add("如需进一步判断，可继续比较续航、尺寸、性能或售后诉求。");
        return suggestions.stream().distinct().toList();
    }

    private String buildComparisonText(ProductKnowledgeCompareVO compareVO) {
        List<String> fragments = new ArrayList<>();
        fragments.add("【商品结构化对比】");
        fragments.add(defaultText(compareVO.getCompareSummary()));
        if (compareVO.getDimensions() != null) {
            compareVO.getDimensions().forEach(dimension -> {
                fragments.add(dimension.getDimensionName() + "：");
                if (dimension.getValues() != null) {
                    dimension.getValues().forEach(value -> fragments.add("- "
                            + defaultText(value.getProductName())
                            + "："
                            + defaultText(value.getValue())
                            + (Boolean.TRUE.equals(value.getHighlight()) ? "（重点）" : "")));
                }
            });
        }
        if (compareVO.getDecisionSuggestions() != null && !compareVO.getDecisionSuggestions().isEmpty()) {
            fragments.add("选购建议：");
            compareVO.getDecisionSuggestions().forEach(item -> fragments.add("- " + item));
        }
        return String.join("\n", fragments);
    }

    private String buildSellingPointSummary(ProductInfoDetailVo detailVo) {
        ProductKnowledgeProperties properties = aiConfigService.getProductKnowledgeConfig();
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
        ProductKnowledgeProperties properties = aiConfigService.getProductKnowledgeConfig();
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

    private ProductKnowledgeVO findCheapestProduct(List<ProductKnowledgeVO> products) {
        return products.stream()
                .filter(product -> product.getMinPrice() != null)
                .min(Comparator.comparing(ProductKnowledgeVO::getMinPrice))
                .orElse(null);
    }

    private ProductKnowledgeVO findTopRatedProduct(List<ProductKnowledgeVO> products) {
        return products.stream()
                .filter(product -> product.getAverageRating() != null && product.getAverageRating() > 0)
                .max(Comparator.comparing(ProductKnowledgeVO::getAverageRating))
                .orElse(null);
    }

    private String formatPriceRange(ProductKnowledgeVO knowledgeVO) {
        if (knowledgeVO.getMinPrice() == null && knowledgeVO.getMaxPrice() == null) {
            return "暂无价格信息";
        }
        if (knowledgeVO.getMinPrice() != null && knowledgeVO.getMaxPrice() != null
                && knowledgeVO.getMinPrice().compareTo(knowledgeVO.getMaxPrice()) == 0) {
            return "¥" + knowledgeVO.getMinPrice().stripTrailingZeros().toPlainString();
        }
        String minPrice = knowledgeVO.getMinPrice() == null ? "-" : knowledgeVO.getMinPrice().stripTrailingZeros().toPlainString();
        String maxPrice = knowledgeVO.getMaxPrice() == null ? "-" : knowledgeVO.getMaxPrice().stripTrailingZeros().toPlainString();
        return "¥" + minPrice + " - ¥" + maxPrice;
    }

    private String formatReviewDimension(ProductKnowledgeVO knowledgeVO) {
        if ((knowledgeVO.getAverageRating() == null || knowledgeVO.getAverageRating() <= 0)
                && (knowledgeVO.getReviewCount() == null || knowledgeVO.getReviewCount() <= 0)) {
            return abbreviate(defaultText(knowledgeVO.getReviewSummary()), 70);
        }
        return String.format(Locale.ROOT, "%.1f 分 / %d 条评价，%s",
                knowledgeVO.getAverageRating() == null ? 0D : knowledgeVO.getAverageRating(),
                knowledgeVO.getReviewCount() == null ? 0 : knowledgeVO.getReviewCount(),
                abbreviate(defaultText(knowledgeVO.getReviewSummary()), 60));
    }

    private String formatKnowledgeTags(ProductKnowledgeVO knowledgeVO) {
        if (knowledgeVO.getKnowledgeTags() == null || knowledgeVO.getKnowledgeTags().isEmpty()) {
            return "暂无标签";
        }
        return String.join("、", knowledgeVO.getKnowledgeTags());
    }

    private String abbreviate(String text, int maxLength) {
        if (StringTools.isEmpty(text) || text.length() <= maxLength) {
            return defaultText(text);
        }
        return text.substring(0, Math.max(maxLength - 3, 1)) + "...";
    }

    private String defaultText(String value) {
        return StringTools.isEmpty(value) ? "暂无" : value;
    }
}
