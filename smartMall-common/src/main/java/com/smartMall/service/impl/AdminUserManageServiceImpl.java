package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.entities.domain.AssistantChatLog;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.ProductReview;
import com.smartMall.entities.domain.RefundInfo;
import com.smartMall.entities.domain.ShoppingCart;
import com.smartMall.entities.domain.UserAccount;
import com.smartMall.entities.domain.UserPreference;
import com.smartMall.entities.dto.AdminUserQueryDTO;
import com.smartMall.entities.dto.AdminUserStatusDTO;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.enums.UserAccountStatusEnum;
import com.smartMall.entities.vo.AdminUserDetailVO;
import com.smartMall.entities.vo.AdminUserListVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.mapper.AssistantChatLogMapper;
import com.smartMall.mapper.OrderInfoMapper;
import com.smartMall.mapper.ProductReviewMapper;
import com.smartMall.mapper.RefundInfoMapper;
import com.smartMall.mapper.ShoppingCartMapper;
import com.smartMall.mapper.UserPreferenceMapper;
import com.smartMall.service.AdminUserManageService;
import com.smartMall.service.UserAccountService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Admin user manage service implementation.
 */
@Service
public class AdminUserManageServiceImpl implements AdminUserManageService {

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserPreferenceMapper userPreferenceMapper;

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private RefundInfoMapper refundInfoMapper;

    @Resource
    private ShoppingCartMapper shoppingCartMapper;

    @Resource
    private ProductReviewMapper productReviewMapper;

    @Resource
    private AssistantChatLogMapper assistantChatLogMapper;

    @Override
    public PageResultVO<AdminUserListVO> loadUserList(AdminUserQueryDTO dto) {
        AdminUserQueryDTO safeQuery = dto == null ? new AdminUserQueryDTO() : dto;
        List<String> candidateUserIds = resolveCandidateUserIds(safeQuery);
        if (candidateUserIds.isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }

        Map<String, UserAccount> accountMap = loadAccountMap(candidateUserIds);
        Map<String, List<OrderInfo>> orderMap = groupByUserId(loadOrders(candidateUserIds), OrderInfo::getUserId);
        Map<String, List<RefundInfo>> refundMap = groupByUserId(loadRefunds(candidateUserIds), RefundInfo::getUserId);
        Map<String, List<ShoppingCart>> cartMap = groupByUserId(loadCartItems(candidateUserIds), ShoppingCart::getUserId);
        Map<String, List<ProductReview>> reviewMap = groupByUserId(loadReviews(candidateUserIds), ProductReview::getUserId);
        Map<String, List<AssistantChatLog>> chatMap = groupByUserId(loadChatLogs(candidateUserIds), AssistantChatLog::getUserId);
        Map<String, UserPreference> preferenceMap = loadPreferences(candidateUserIds).stream()
                .collect(Collectors.toMap(UserPreference::getUserId, Function.identity(), (left, right) -> left));

        List<AdminUserListVO> records = candidateUserIds.stream()
                .filter(userId -> hasUserEvidence(userId,
                        accountMap.get(userId),
                        orderMap.get(userId),
                        refundMap.get(userId),
                        cartMap.get(userId),
                        reviewMap.get(userId),
                        chatMap.get(userId),
                        preferenceMap.get(userId)))
                .map(userId -> buildUserListVO(userId,
                        accountMap.get(userId),
                        orderMap.getOrDefault(userId, List.of()),
                        refundMap.getOrDefault(userId, List.of()),
                        cartMap.getOrDefault(userId, List.of()),
                        reviewMap.getOrDefault(userId, List.of()),
                        chatMap.getOrDefault(userId, List.of())))
                .filter(item -> matchesKeyword(item, safeQuery))
                .filter(item -> safeQuery.getStatus() == null || Objects.equals(item.getStatus(), safeQuery.getStatus()))
                .sorted(Comparator.comparing(AdminUserListVO::getLastActiveTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AdminUserListVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        if (records.isEmpty()) {
            return PageResultVO.empty(safeQuery.getPageNo(), safeQuery.getPageSize());
        }

        int pageNo = safeQuery.getPageNo();
        int pageSize = safeQuery.getPageSize();
        int fromIndex = Math.max(0, (pageNo - 1) * pageSize);
        if (fromIndex >= records.size()) {
            return PageResultVO.empty(pageNo, pageSize);
        }
        int toIndex = Math.min(records.size(), fromIndex + pageSize);
        return new PageResultVO<>(pageNo, pageSize, (long) records.size(), records.subList(fromIndex, toIndex));
    }

    @Override
    public AdminUserDetailVO getUserDetail(String userId) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "userId is required");
        }
        UserAccount userAccount = userAccountService.getById(userId);
        List<OrderInfo> orders = loadOrders(List.of(userId));
        List<RefundInfo> refunds = loadRefunds(List.of(userId));
        List<ShoppingCart> cartItems = loadCartItems(List.of(userId));
        List<ProductReview> reviews = loadReviews(List.of(userId));
        List<AssistantChatLog> chatLogs = loadChatLogs(List.of(userId));
        UserPreference preference = loadUserPreference(userId);
        if (userAccount == null && orders.isEmpty() && refunds.isEmpty() && cartItems.isEmpty()
                && reviews.isEmpty() && chatLogs.isEmpty() && preference == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "user not found");
        }
        return buildUserDetailVO(userId, userAccount, orders, refunds, cartItems, reviews, chatLogs, preference);
    }

    @Override
    public void updateUserStatus(AdminUserStatusDTO dto) {
        UserAccountStatusEnum statusEnum = UserAccountStatusEnum.getByStatus(dto.getStatus());
        if (statusEnum == null) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "user status is invalid");
        }
        Date now = new Date();
        UserAccount userAccount = userAccountService.getById(dto.getUserId());
        if (userAccount == null) {
            userAccount = new UserAccount();
            userAccount.setUserId(dto.getUserId());
            userAccount.setUsername(dto.getUserId());
            userAccount.setNickname(dto.getUserId());
            userAccount.setStatus(dto.getStatus());
            userAccount.setCreateTime(now);
            userAccount.setUpdateTime(now);
            userAccountService.save(userAccount);
            return;
        }
        userAccount.setStatus(dto.getStatus());
        userAccount.setUpdateTime(now);
        userAccountService.updateById(userAccount);
    }

    private List<String> resolveCandidateUserIds(AdminUserQueryDTO query) {
        if (StringTools.isNotEmpty(query.getUserId())) {
            return List.of(query.getUserId().trim());
        }
        Set<String> userIds = new LinkedHashSet<>();
        userIds.addAll(userAccountService.list(new LambdaQueryWrapper<UserAccount>()
                        .select(UserAccount::getUserId))
                .stream()
                .map(UserAccount::getUserId)
                .filter(StringTools::isNotEmpty)
                .toList());
        userIds.addAll(orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                        .select(OrderInfo::getUserId))
                .stream()
                .map(OrderInfo::getUserId)
                .filter(StringTools::isNotEmpty)
                .toList());
        userIds.addAll(refundInfoMapper.selectList(new LambdaQueryWrapper<RefundInfo>()
                        .select(RefundInfo::getUserId))
                .stream()
                .map(RefundInfo::getUserId)
                .filter(StringTools::isNotEmpty)
                .toList());
        userIds.addAll(shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                        .select(ShoppingCart::getUserId))
                .stream()
                .map(ShoppingCart::getUserId)
                .filter(StringTools::isNotEmpty)
                .toList());
        userIds.addAll(productReviewMapper.selectList(new LambdaQueryWrapper<ProductReview>()
                        .select(ProductReview::getUserId))
                .stream()
                .map(ProductReview::getUserId)
                .filter(StringTools::isNotEmpty)
                .toList());
        userIds.addAll(assistantChatLogMapper.selectList(new LambdaQueryWrapper<AssistantChatLog>()
                        .select(AssistantChatLog::getUserId))
                .stream()
                .map(AssistantChatLog::getUserId)
                .filter(StringTools::isNotEmpty)
                .toList());
        userIds.addAll(userPreferenceMapper.selectList(new LambdaQueryWrapper<UserPreference>()
                        .select(UserPreference::getUserId))
                .stream()
                .map(UserPreference::getUserId)
                .filter(StringTools::isNotEmpty)
                .toList());
        return new ArrayList<>(userIds);
    }

    private Map<String, UserAccount> loadAccountMap(List<String> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userAccountService.listByIds(userIds).stream()
                .collect(Collectors.toMap(UserAccount::getUserId, Function.identity(), (left, right) -> left));
    }

    private List<OrderInfo> loadOrders(List<String> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .in(OrderInfo::getUserId, userIds)
                .orderByDesc(OrderInfo::getCreateTime));
    }

    private List<RefundInfo> loadRefunds(List<String> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return refundInfoMapper.selectList(new LambdaQueryWrapper<RefundInfo>()
                .in(RefundInfo::getUserId, userIds)
                .orderByDesc(RefundInfo::getCreateTime));
    }

    private List<ShoppingCart> loadCartItems(List<String> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .in(ShoppingCart::getUserId, userIds)
                .orderByDesc(ShoppingCart::getUpdateTime));
    }

    private List<ProductReview> loadReviews(List<String> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return productReviewMapper.selectList(new LambdaQueryWrapper<ProductReview>()
                .in(ProductReview::getUserId, userIds)
                .orderByDesc(ProductReview::getCreateTime));
    }

    private List<AssistantChatLog> loadChatLogs(List<String> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return assistantChatLogMapper.selectList(new LambdaQueryWrapper<AssistantChatLog>()
                .in(AssistantChatLog::getUserId, userIds)
                .orderByDesc(AssistantChatLog::getCreateTime));
    }

    private UserPreference loadUserPreference(String userId) {
        return userPreferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, userId)
                .last("LIMIT 1"));
    }

    private List<UserPreference> loadPreferences(List<String> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return userPreferenceMapper.selectList(new LambdaQueryWrapper<UserPreference>()
                .in(UserPreference::getUserId, userIds));
    }

    private <T> Map<String, List<T>> groupByUserId(List<T> source, Function<T, String> userIdGetter) {
        return source.stream()
                .filter(item -> StringTools.isNotEmpty(userIdGetter.apply(item)))
                .collect(Collectors.groupingBy(userIdGetter));
    }

    private AdminUserListVO buildUserListVO(String userId,
                                            UserAccount account,
                                            List<OrderInfo> orders,
                                            List<RefundInfo> refunds,
                                            List<ShoppingCart> cartItems,
                                            List<ProductReview> reviews,
                                            List<AssistantChatLog> chatLogs) {
        AdminUserListVO vo = new AdminUserListVO();
        vo.setUserId(userId);
        vo.setUsername(resolveUsername(userId, account));
        vo.setNickname(resolveNickname(userId, account));
        vo.setPhone(resolvePhone(account, orders));
        vo.setStatus(resolveStatus(account));
        vo.setStatusDesc(resolveStatusDesc(vo.getStatus()));
        vo.setOrderCount((long) orders.size());
        vo.setTotalOrderAmount(sumOrderAmount(orders));
        vo.setRefundCount((long) refunds.size());
        vo.setTotalRefundAmount(sumRefundAmount(refunds));
        vo.setCartItemCount(cartItems.size());
        vo.setLastActiveTime(resolveLastActiveTime(account, orders, refunds, cartItems, reviews, chatLogs));
        vo.setCreateTime(resolveCreateTime(account, orders, refunds, cartItems, reviews, chatLogs));
        return vo;
    }

    private AdminUserDetailVO buildUserDetailVO(String userId,
                                                UserAccount account,
                                                List<OrderInfo> orders,
                                                List<RefundInfo> refunds,
                                                List<ShoppingCart> cartItems,
                                                List<ProductReview> reviews,
                                                List<AssistantChatLog> chatLogs,
                                                UserPreference preference) {
        AdminUserDetailVO vo = new AdminUserDetailVO();
        vo.setUserId(userId);
        vo.setUsername(resolveUsername(userId, account));
        vo.setNickname(resolveNickname(userId, account));
        vo.setAvatar(account == null ? null : account.getAvatar());
        vo.setPhone(resolvePhone(account, orders));
        vo.setStatus(resolveStatus(account));
        vo.setStatusDesc(resolveStatusDesc(vo.getStatus()));
        vo.setRemark(account == null ? null : account.getRemark());
        vo.setOrderCount((long) orders.size());
        vo.setTotalOrderAmount(sumOrderAmount(orders));
        vo.setRefundCount((long) refunds.size());
        vo.setTotalRefundAmount(sumRefundAmount(refunds));
        vo.setCartItemCount(cartItems.size());
        vo.setReviewCount(reviews.size());
        vo.setAverageRating(preference == null ? null : preference.getAverageRating());
        vo.setFavoriteCategoryNames(splitCommaValue(preference == null ? null : preference.getFavoriteCategoryNames()));
        vo.setPreferenceTags(splitCommaValue(preference == null ? null : preference.getPreferenceTags()));
        vo.setRecentSearchKeywords(splitCommaValue(preference == null ? null : preference.getRecentSearchKeywords()));
        vo.setLastOrderTime(maxDate(orders.stream().map(OrderInfo::getCreateTime).toList()));
        vo.setLastRefundTime(maxDate(refunds.stream().map(RefundInfo::getCreateTime).toList()));
        vo.setLastChatTime(maxDate(chatLogs.stream().map(AssistantChatLog::getCreateTime).toList()));
        vo.setLastActiveTime(resolveLastActiveTime(account, orders, refunds, cartItems, reviews, chatLogs));
        vo.setCreateTime(resolveCreateTime(account, orders, refunds, cartItems, reviews, chatLogs));
        vo.setUpdateTime(account == null ? (preference == null ? null : preference.getUpdateTime()) : account.getUpdateTime());
        return vo;
    }

    private boolean matchesKeyword(AdminUserListVO item, AdminUserQueryDTO query) {
        if (StringTools.isNotEmpty(query.getPhone()) && (item.getPhone() == null || !item.getPhone().contains(query.getPhone()))) {
            return false;
        }
        if (StringTools.isEmpty(query.getKeyword())) {
            return true;
        }
        String keyword = query.getKeyword();
        return contains(item.getUserId(), keyword)
                || contains(item.getUsername(), keyword)
                || contains(item.getNickname(), keyword);
    }

    private boolean hasUserEvidence(String userId,
                                    UserAccount account,
                                    List<OrderInfo> orders,
                                    List<RefundInfo> refunds,
                                    List<ShoppingCart> cartItems,
                                    List<ProductReview> reviews,
                                    List<AssistantChatLog> chatLogs,
                                    UserPreference preference) {
        return StringTools.isNotEmpty(userId) && (account != null
                || (orders != null && !orders.isEmpty())
                || (refunds != null && !refunds.isEmpty())
                || (cartItems != null && !cartItems.isEmpty())
                || (reviews != null && !reviews.isEmpty())
                || (chatLogs != null && !chatLogs.isEmpty())
                || preference != null);
    }

    private boolean contains(String source, String keyword) {
        return StringTools.isNotEmpty(source) && StringTools.isNotEmpty(keyword) && source.contains(keyword);
    }

    private String resolveUsername(String userId, UserAccount account) {
        if (account != null && StringTools.isNotEmpty(account.getUsername())) {
            return account.getUsername();
        }
        return userId;
    }

    private String resolveNickname(String userId, UserAccount account) {
        if (account != null && StringTools.isNotEmpty(account.getNickname())) {
            return account.getNickname();
        }
        return userId;
    }

    private String resolvePhone(UserAccount account, List<OrderInfo> orders) {
        if (account != null && StringTools.isNotEmpty(account.getPhone())) {
            return account.getPhone();
        }
        return orders.stream()
                .sorted(Comparator.comparing(OrderInfo::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(OrderInfo::getReceiverPhone)
                .filter(StringTools::isNotEmpty)
                .findFirst()
                .orElse(null);
    }

    private Integer resolveStatus(UserAccount account) {
        return account == null || account.getStatus() == null
                ? UserAccountStatusEnum.ENABLED.getStatus() : account.getStatus();
    }

    private String resolveStatusDesc(Integer status) {
        UserAccountStatusEnum statusEnum = UserAccountStatusEnum.getByStatus(status);
        return statusEnum == null ? "unknown" : statusEnum.getDesc();
    }

    private BigDecimal sumOrderAmount(List<OrderInfo> orders) {
        return orders.stream()
                .map(OrderInfo::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumRefundAmount(List<RefundInfo> refunds) {
        return refunds.stream()
                .map(RefundInfo::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Date resolveLastActiveTime(UserAccount account,
                                       List<OrderInfo> orders,
                                       List<RefundInfo> refunds,
                                       List<ShoppingCart> cartItems,
                                       List<ProductReview> reviews,
                                       List<AssistantChatLog> chatLogs) {
        List<Date> candidates = new ArrayList<>();
        if (account != null) {
            candidates.add(account.getLastActiveTime());
            candidates.add(account.getUpdateTime());
            candidates.add(account.getCreateTime());
        }
        candidates.addAll(orders.stream().map(OrderInfo::getUpdateTime).toList());
        candidates.addAll(refunds.stream().map(RefundInfo::getUpdateTime).toList());
        candidates.addAll(cartItems.stream().map(ShoppingCart::getUpdateTime).toList());
        candidates.addAll(reviews.stream().map(ProductReview::getCreateTime).toList());
        candidates.addAll(chatLogs.stream().map(AssistantChatLog::getCreateTime).toList());
        return maxDate(candidates);
    }

    private Date resolveCreateTime(UserAccount account,
                                   List<OrderInfo> orders,
                                   List<RefundInfo> refunds,
                                   List<ShoppingCart> cartItems,
                                   List<ProductReview> reviews,
                                   List<AssistantChatLog> chatLogs) {
        if (account != null && account.getCreateTime() != null) {
            return account.getCreateTime();
        }
        List<Date> candidates = new ArrayList<>();
        candidates.addAll(orders.stream().map(OrderInfo::getCreateTime).toList());
        candidates.addAll(refunds.stream().map(RefundInfo::getCreateTime).toList());
        candidates.addAll(cartItems.stream().map(ShoppingCart::getCreateTime).toList());
        candidates.addAll(reviews.stream().map(ProductReview::getCreateTime).toList());
        candidates.addAll(chatLogs.stream().map(AssistantChatLog::getCreateTime).toList());
        return minDate(candidates);
    }

    private Date maxDate(List<Date> dates) {
        return dates.stream()
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);
    }

    private Date minDate(List<Date> dates) {
        return dates.stream()
                .filter(Objects::nonNull)
                .min(Date::compareTo)
                .orElse(null);
    }

    private List<String> splitCommaValue(String value) {
        if (StringTools.isEmpty(value)) {
            return List.of();
        }
        return List.of(value.split(",")).stream()
                .map(String::trim)
                .filter(StringTools::isNotEmpty)
                .toList();
    }
}
