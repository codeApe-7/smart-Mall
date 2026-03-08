package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.config.UserPreferenceProperties;
import com.smartMall.entities.domain.AssistantChatLog;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.OrderItem;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.ProductReview;
import com.smartMall.entities.domain.ShoppingCart;
import com.smartMall.entities.domain.SysCategory;
import com.smartMall.entities.domain.UserPreference;
import com.smartMall.entities.enums.AssistantIntentEnum;
import com.smartMall.entities.enums.OrderStatusEnum;
import com.smartMall.entities.vo.UserPreferenceVO;
import com.smartMall.mapper.UserPreferenceMapper;
import com.smartMall.service.AssistantChatLogService;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.OrderItemService;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductReviewService;
import com.smartMall.service.ShoppingCartService;
import com.smartMall.service.SysCategoryService;
import com.smartMall.service.UserPreferenceService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * 用户偏好档案服务实现。
 */
@Service
@Slf4j
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference>
        implements UserPreferenceService {

    @Resource
    private UserPreferenceProperties preferenceProperties;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private SysCategoryService sysCategoryService;

    @Resource
    private AssistantChatLogService assistantChatLogService;

    @Resource
    private ProductReviewService productReviewService;

    @Resource
    private ShoppingCartService shoppingCartService;

    @Override
    public UserPreferenceVO getUserPreference(String userId) {
        UserPreference preference = getByUserId(userId);
        if (preference == null) {
            return buildEmptyPreferenceVO(userId);
        }
        return convertToVO(preference);
    }

    @Override
    public UserPreferenceVO refreshUserPreference(String userId) {
        UserPreference preference = buildUserPreference(userId);
        UserPreference existing = getByUserId(userId);
        if (existing != null) {
            preference.setPreferenceId(existing.getPreferenceId());
            preference.setCreateTime(existing.getCreateTime());
            this.updateById(preference);
        } else {
            preference.setPreferenceId(StringTools.getRandomNumber(LENGTH_32));
            preference.setCreateTime(new Date());
            this.save(preference);
        }
        log.info("refreshed user preference, userId={}, preferenceId={}", userId, preference.getPreferenceId());
        return convertToVO(preference);
    }

    private UserPreference getByUserId(String userId) {
        return this.getOne(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, userId)
                .last("LIMIT 1"));
    }

    private UserPreference buildUserPreference(String userId) {
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setUpdateTime(new Date());

        // 1. Purchase behavior: recent orders (non-canceled, non-refunded)
        Date sinceDate = computeSinceDate(preferenceProperties.getRecentOrderDays());
        List<Integer> excludedStatuses = List.of(
                OrderStatusEnum.CANCELED.getStatus(),
                OrderStatusEnum.REFUND_REQUESTED.getStatus(),
                OrderStatusEnum.REFUNDED.getStatus());
        List<OrderInfo> recentOrders = orderInfoService.list(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .notIn(OrderInfo::getOrderStatus, excludedStatuses)
                .ge(OrderInfo::getCreateTime, sinceDate)
                .orderByDesc(OrderInfo::getCreateTime));
        preference.setOrderCount(recentOrders.size());

        // 2. Collect order items for category/price analysis
        List<String> orderIds = recentOrders.stream().map(OrderInfo::getOrderId).toList();
        List<OrderItem> orderItems = orderIds.isEmpty() ? List.of() : orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));

        List<String> purchasedProductIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .toList();
        preference.setRecentProductIds(joinList(purchasedProductIds));

        // 3. Category preference: frequency sorted top N
        Map<String, Long> categoryFrequency = new LinkedHashMap<>();
        if (!purchasedProductIds.isEmpty()) {
            List<ProductInfo> purchasedProducts = productInfoService.listByIds(purchasedProductIds);
            for (ProductInfo product : purchasedProducts) {
                if (StringTools.isNotEmpty(product.getCategoryId())) {
                    categoryFrequency.merge(product.getCategoryId(), 1L, Long::sum);
                }
            }
        }

        // 4. Shopping cart signal: supplement category preference
        List<ShoppingCart> cartItems = shoppingCartService.list(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId));
        List<String> cartProductIds = cartItems.stream()
                .map(ShoppingCart::getProductId)
                .distinct()
                .toList();
        if (!cartProductIds.isEmpty()) {
            List<ProductInfo> cartProducts = productInfoService.listByIds(cartProductIds);
            for (ProductInfo product : cartProducts) {
                if (StringTools.isNotEmpty(product.getCategoryId())) {
                    categoryFrequency.merge(product.getCategoryId(), 1L, Long::sum);
                }
            }
        }

        // Sort by frequency descending, take top N
        List<String> topCategoryIds = categoryFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(preferenceProperties.getTopCategoryCount())
                .map(Map.Entry::getKey)
                .toList();
        preference.setFavoriteCategoryIds(joinList(topCategoryIds));

        // Resolve category names
        if (!topCategoryIds.isEmpty()) {
            List<SysCategory> categories = sysCategoryService.listByIds(topCategoryIds);
            Map<String, String> categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(SysCategory::getCategoryId, SysCategory::getCategoryName, (a, b) -> a));
            List<String> categoryNames = topCategoryIds.stream()
                    .map(id -> categoryNameMap.getOrDefault(id, ""))
                    .filter(StringTools::isNotEmpty)
                    .toList();
            preference.setFavoriteCategoryNames(joinList(categoryNames));
        }

        // 5. Price preference: min and max of purchased prices
        List<BigDecimal> prices = orderItems.stream()
                .map(OrderItem::getPrice)
                .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (!prices.isEmpty()) {
            preference.setMinPricePreference(prices.stream().min(BigDecimal::compareTo).orElse(null));
            preference.setMaxPricePreference(prices.stream().max(BigDecimal::compareTo).orElse(null));
        }

        // 6. Search history: from assistant_chat_log with intent=PRODUCT_SEARCH
        List<AssistantChatLog> searchLogs = assistantChatLogService.list(new LambdaQueryWrapper<AssistantChatLog>()
                .eq(AssistantChatLog::getUserId, userId)
                .eq(AssistantChatLog::getIntentType, AssistantIntentEnum.PRODUCT_SEARCH.getCode())
                .orderByDesc(AssistantChatLog::getCreateTime)
                .last("LIMIT 50"));
        Set<String> searchKeywords = new LinkedHashSet<>();
        for (AssistantChatLog chatLog : searchLogs) {
            String text = chatLog.getRequestText();
            if (StringTools.isNotEmpty(text)) {
                String cleaned = text.trim();
                if (!cleaned.isEmpty() && cleaned.length() <= 50) {
                    searchKeywords.add(cleaned);
                }
                if (searchKeywords.size() >= preferenceProperties.getRecentSearchKeywordCount()) {
                    break;
                }
            }
        }
        preference.setRecentSearchKeywords(joinList(new ArrayList<>(searchKeywords)));

        // 7. Review data: average rating and count
        List<ProductReview> reviews = productReviewService.list(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getUserId, userId));
        preference.setReviewCount(reviews.size());
        if (!reviews.isEmpty()) {
            double avgRating = reviews.stream()
                    .mapToInt(ProductReview::getRating)
                    .average()
                    .orElse(0.0);
            preference.setAverageRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
        }

        // 8. Preference tags: category names + high-frequency search keywords
        Set<String> tags = new LinkedHashSet<>();
        if (StringTools.isNotEmpty(preference.getFavoriteCategoryNames())) {
            tags.addAll(splitToList(preference.getFavoriteCategoryNames()));
        }
        int tagSlots = 10 - tags.size();
        if (tagSlots > 0) {
            searchKeywords.stream().limit(tagSlots).forEach(tags::add);
        }
        preference.setPreferenceTags(joinList(new ArrayList<>(tags)));

        return preference;
    }

    private UserPreferenceVO convertToVO(UserPreference preference) {
        UserPreferenceVO vo = new UserPreferenceVO();
        vo.setPreferenceId(preference.getPreferenceId());
        vo.setUserId(preference.getUserId());
        vo.setFavoriteCategoryIds(splitToList(preference.getFavoriteCategoryIds()));
        vo.setFavoriteCategoryNames(splitToList(preference.getFavoriteCategoryNames()));
        vo.setMinPricePreference(preference.getMinPricePreference());
        vo.setMaxPricePreference(preference.getMaxPricePreference());
        vo.setRecentSearchKeywords(splitToList(preference.getRecentSearchKeywords()));
        vo.setRecentProductIds(splitToList(preference.getRecentProductIds()));
        vo.setAverageRating(preference.getAverageRating());
        vo.setPreferenceTags(splitToList(preference.getPreferenceTags()));
        vo.setOrderCount(preference.getOrderCount());
        vo.setReviewCount(preference.getReviewCount());
        vo.setCreateTime(preference.getCreateTime());
        vo.setUpdateTime(preference.getUpdateTime());
        return vo;
    }

    private UserPreferenceVO buildEmptyPreferenceVO(String userId) {
        UserPreferenceVO vo = new UserPreferenceVO();
        vo.setUserId(userId);
        vo.setFavoriteCategoryIds(List.of());
        vo.setFavoriteCategoryNames(List.of());
        vo.setRecentSearchKeywords(List.of());
        vo.setRecentProductIds(List.of());
        vo.setPreferenceTags(List.of());
        vo.setOrderCount(0);
        vo.setReviewCount(0);
        return vo;
    }

    private Date computeSinceDate(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return Date.from(since.atZone(ZoneId.systemDefault()).toInstant());
    }

    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }

    private List<String> splitToList(String str) {
        if (str == null || str.isBlank()) {
            return List.of();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
