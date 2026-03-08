package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartMall.entities.domain.AssistantChatLog;
import com.smartMall.entities.dto.AssistantChatRequestDTO;
import com.smartMall.entities.dto.AssistantHistoryQueryDTO;
import com.smartMall.entities.dto.ConfirmReceiveDTO;
import com.smartMall.entities.dto.OrderCancelDTO;
import com.smartMall.entities.dto.OrderQueryDTO;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.dto.RefundApplyDTO;
import com.smartMall.entities.dto.ReviewSubmitDTO;
import com.smartMall.entities.enums.AssistantIntentEnum;
import com.smartMall.entities.vo.AssistantChatHistoryVO;
import com.smartMall.entities.vo.AssistantChatPayloadVO;
import com.smartMall.entities.vo.AssistantChatResponseVO;
import com.smartMall.entities.vo.AssistantOperationVO;
import com.smartMall.entities.vo.OrderDetailVO;
import com.smartMall.entities.vo.OrderInfoListVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.entities.vo.ProductReviewVO;
import com.smartMall.entities.vo.RefundInfoVO;
import com.smartMall.entities.vo.ShippingInfoVO;
import com.smartMall.service.AssistantChatLogService;
import com.smartMall.service.MallAssistantService;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductReviewService;
import com.smartMall.service.RefundInfoService;
import com.smartMall.service.ShippingInfoService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * Intelligent shopping assistant service implementation.
 */
@Service
@Slf4j
public class MallAssistantServiceImpl implements MallAssistantService {

    private static final int DEFAULT_PRODUCT_PAGE_SIZE = 5;
    private static final int DEFAULT_ORDER_PAGE_SIZE = 5;
    private static final int DEFAULT_RECOMMEND_LIMIT = 4;
    private static final int MAX_SUMMARY_LENGTH = 1000;
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("\\b[pP][a-zA-Z0-9]+\\b");
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("\\b[oO][a-zA-Z0-9]+\\b");
    private static final List<String> DEFAULT_SUGGESTIONS = List.of(
            "帮我推荐几款热销商品",
            "帮我找手机",
            "查询我的订单",
            "查看订单详情 o10001");

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private AssistantChatLogService assistantChatLogService;

    @Resource
    private RefundInfoService refundInfoService;

    @Resource
    private ShippingInfoService shippingInfoService;

    @Resource
    private ProductReviewService productReviewService;

    @Override
    public AssistantChatResponseVO chat(AssistantChatRequestDTO dto) {
        String sessionId = normalizeSessionId(dto.getSessionId());
        AssistantIntentEnum intent = resolveIntent(dto);
        AssistantChatResponseVO response = switch (intent) {
            case PRODUCT_RECOMMEND -> handleRecommend(dto, sessionId, intent);
            case PRODUCT_DETAIL -> handleProductDetail(dto, sessionId, intent);
            case ORDER_LIST -> handleOrderList(dto, sessionId, intent);
            case ORDER_DETAIL -> handleOrderDetail(dto, sessionId, intent);
            case ORDER_CANCEL -> handleCancelOrder(dto, sessionId, intent);
            case REFUND_APPLY -> handleApplyRefund(dto, sessionId, intent);
            case REFUND_DETAIL -> handleRefundDetail(dto, sessionId, intent);
            case RECEIVE_CONFIRM -> handleConfirmReceive(dto, sessionId, intent);
            case ORDER_REVIEW_QUERY -> handleOrderReviews(dto, sessionId, intent);
            case REVIEW_SUBMIT -> handleSubmitReview(dto, sessionId, intent);
            case PRODUCT_SEARCH, UNKNOWN -> handleProductSearch(dto, sessionId, intent);
        };
        saveChatLog(dto, response);
        return response;
    }

    @Override
    public PageResultVO<AssistantChatHistoryVO> loadHistory(AssistantHistoryQueryDTO dto) {
        LambdaQueryWrapper<AssistantChatLog> queryWrapper = new LambdaQueryWrapper<AssistantChatLog>()
                .eq(AssistantChatLog::getUserId, dto.getUserId())
                .eq(StringTools.isNotEmpty(dto.getSessionId()), AssistantChatLog::getSessionId, dto.getSessionId())
                .orderByDesc(AssistantChatLog::getCreateTime);
        Page<AssistantChatLog> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        assistantChatLogService.page(page, queryWrapper);
        if (page.getRecords().isEmpty()) {
            return PageResultVO.empty(dto.getPageNo(), dto.getPageSize());
        }
        List<AssistantChatHistoryVO> records = page.getRecords().stream()
                .map(this::buildHistoryVO)
                .toList();
        return new PageResultVO<>(dto.getPageNo(), dto.getPageSize(), page.getTotal(), records);
    }

    private AssistantChatResponseVO handleProductSearch(AssistantChatRequestDTO dto, String sessionId,
                                                        AssistantIntentEnum intent) {
        String keyword = extractProductKeyword(dto.getMessage());
        if (StringTools.isEmpty(keyword)) {
            return handleRecommend(dto, sessionId, AssistantIntentEnum.PRODUCT_RECOMMEND);
        }
        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setPageNo(1);
        queryDTO.setPageSize(DEFAULT_PRODUCT_PAGE_SIZE);
        queryDTO.setProductName(keyword);
        PageResultVO<ProductInfoListVO> productPage = productInfoService.loadVisibleProductList(queryDTO);

        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setProductPage(productPage);

        String reply = productPage.getRecords().isEmpty()
                ? "没有找到和\"" + keyword + "\"相关的在售商品，你可以换个关键词或者让我直接推荐。"
                : "为你找到 " + productPage.getTotalCount() + " 个和\"" + keyword + "\"相关的在售商品，先给你前 "
                + productPage.getRecords().size() + " 个。";
        return buildResponse(sessionId, intent, reply, payload, List.of(
                "查看商品详情 " + firstProductId(productPage.getRecords()),
                "帮我推荐几款热销商品",
                "查询我的订单"));
    }

    private AssistantChatResponseVO handleRecommend(AssistantChatRequestDTO dto, String sessionId,
                                                    AssistantIntentEnum intent) {
        List<ProductInfoListVO> recommendProducts = productInfoService.loadRecommendProducts(DEFAULT_RECOMMEND_LIMIT);
        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setProductPage(new PageResultVO<>(1, DEFAULT_RECOMMEND_LIMIT,
                (long) recommendProducts.size(), recommendProducts));
        String reply = recommendProducts.isEmpty()
                ? "当前还没有可推荐的在售商品。"
                : "先给你推荐几款商品，你也可以继续告诉我想买什么类型。";
        return buildResponse(sessionId, intent, reply, payload, List.of(
                "帮我找手机",
                "帮我找运动鞋",
                "查看商品详情 " + firstProductId(recommendProducts)));
    }

    private AssistantChatResponseVO handleProductDetail(AssistantChatRequestDTO dto, String sessionId,
                                                        AssistantIntentEnum intent) {
        String productId = extractProductId(dto);
        ProductInfoDetailVo detailVo = productInfoService.getVisibleProductDetail(productId);
        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setProductDetail(detailVo);
        String productName = detailVo.getProductInfo() == null ? productId : detailVo.getProductInfo().getProductName();
        return buildResponse(sessionId, intent,
                "已为你找到商品详情：" + productName + "。如果你需要，我还可以继续推荐相似商品。",
                payload,
                List.of("帮我推荐相似商品", "查询我的订单", "帮我推荐几款热销商品"));
    }

    private AssistantChatResponseVO handleOrderList(AssistantChatRequestDTO dto, String sessionId,
                                                    AssistantIntentEnum intent) {
        OrderQueryDTO queryDTO = new OrderQueryDTO();
        queryDTO.setUserId(dto.getUserId());
        queryDTO.setPageNo(1);
        queryDTO.setPageSize(DEFAULT_ORDER_PAGE_SIZE);
        queryDTO.setOrderStatus(resolveOrderStatus(dto.getMessage()));
        PageResultVO<OrderInfoListVO> orderPage = orderInfoService.loadOrderList(queryDTO);

        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setOrderPage(orderPage);

        String reply = orderPage.getRecords().isEmpty()
                ? "当前没有匹配条件的订单记录。"
                : "已为你找到 " + orderPage.getTotalCount() + " 条订单记录，先展示最近的 "
                + orderPage.getRecords().size() + " 条。";
        return buildResponse(sessionId, intent, reply, payload, List.of(
                "查看订单详情 " + firstOrderId(orderPage.getRecords()),
                "取消订单 " + firstCancelableOrderId(orderPage.getRecords()),
                "帮我推荐几款热销商品"));
    }

    private AssistantChatResponseVO handleOrderDetail(AssistantChatRequestDTO dto, String sessionId,
                                                      AssistantIntentEnum intent) {
        String orderId = extractOrderId(dto);
        OrderDetailVO orderDetailVO = orderInfoService.getOrderDetail(dto.getUserId(), orderId);
        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setOrderDetail(orderDetailVO);
        return buildResponse(sessionId, intent,
                "已为你找到订单 " + orderDetailVO.getOrderNo() + " 的详情。",
                payload,
                List.of("取消订单 " + orderId, "查询我的订单", "帮我推荐几款热销商品"));
    }

    private AssistantChatResponseVO handleCancelOrder(AssistantChatRequestDTO dto, String sessionId,
                                                      AssistantIntentEnum intent) {
        String orderId = extractOrderId(dto);
        OrderCancelDTO cancelDTO = new OrderCancelDTO();
        cancelDTO.setUserId(dto.getUserId());
        cancelDTO.setOrderId(orderId);
        orderInfoService.cancelOrder(cancelDTO);
        OrderDetailVO orderDetailVO = orderInfoService.getOrderDetail(dto.getUserId(), orderId);

        AssistantOperationVO operationVO = new AssistantOperationVO();
        operationVO.setAction(intent.getCode());
        operationVO.setOrderId(orderId);
        operationVO.setOrderStatus(orderDetailVO.getOrderStatus());
        operationVO.setOrderStatusDesc(orderDetailVO.getOrderStatusDesc());
        operationVO.setMessage("订单已取消");

        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setOrderDetail(orderDetailVO);
        payload.setOperation(operationVO);

        return buildResponse(sessionId, intent,
                "订单 " + orderDetailVO.getOrderNo() + " 已取消。",
                payload,
                List.of("查询我的订单", "帮我推荐几款热销商品", "查看订单详情 " + orderId));
    }

    private AssistantChatResponseVO handleApplyRefund(AssistantChatRequestDTO dto, String sessionId,
                                                      AssistantIntentEnum intent) {
        String orderId = extractOrderId(dto);
        RefundApplyDTO refundApplyDTO = new RefundApplyDTO();
        refundApplyDTO.setUserId(dto.getUserId());
        refundApplyDTO.setOrderId(orderId);
        refundApplyDTO.setRefundReason(dto.getRefundReason());
        RefundInfoVO refundInfoVO = refundInfoService.applyRefund(refundApplyDTO);

        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setRefundInfo(refundInfoVO);

        AssistantOperationVO operationVO = new AssistantOperationVO();
        operationVO.setAction(intent.getCode());
        operationVO.setOrderId(orderId);
        operationVO.setMessage("退款申请已提交");
        payload.setOperation(operationVO);

        return buildResponse(sessionId, intent,
                "订单 " + refundInfoVO.getOrderNo() + " 的退款申请已提交。",
                payload,
                List.of("查询退款详情 " + orderId, "查询我的订单", "帮我推荐几款热销商品"));
    }

    private AssistantChatResponseVO handleRefundDetail(AssistantChatRequestDTO dto, String sessionId,
                                                       AssistantIntentEnum intent) {
        String orderId = extractOrderId(dto);
        RefundInfoVO refundInfoVO = refundInfoService.getRefundDetail(dto.getUserId(), orderId);

        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setRefundInfo(refundInfoVO);

        return buildResponse(sessionId, intent,
                "已为你找到订单 " + refundInfoVO.getOrderNo() + " 的退款记录。",
                payload,
                List.of("查询我的订单", "查看订单详情 " + orderId, "帮我推荐几款热销商品"));
    }

    private AssistantChatResponseVO handleConfirmReceive(AssistantChatRequestDTO dto, String sessionId,
                                                         AssistantIntentEnum intent) {
        String orderId = extractOrderId(dto);
        ConfirmReceiveDTO confirmReceiveDTO = new ConfirmReceiveDTO();
        confirmReceiveDTO.setUserId(dto.getUserId());
        confirmReceiveDTO.setOrderId(orderId);
        shippingInfoService.confirmReceive(confirmReceiveDTO);
        ShippingInfoVO shippingInfoVO = shippingInfoService.getShippingDetail(dto.getUserId(), orderId);

        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setShippingInfo(shippingInfoVO);

        AssistantOperationVO operationVO = new AssistantOperationVO();
        operationVO.setAction(intent.getCode());
        operationVO.setOrderId(orderId);
        operationVO.setMessage("订单已确认收货");
        payload.setOperation(operationVO);

        return buildResponse(sessionId, intent,
                "订单 " + shippingInfoVO.getOrderNo() + " 已确认收货。",
                payload,
                List.of("查看订单评价 " + orderId, "查询我的订单", "帮我推荐几款热销商品"));
    }

    private AssistantChatResponseVO handleOrderReviews(AssistantChatRequestDTO dto, String sessionId,
                                                       AssistantIntentEnum intent) {
        String orderId = extractOrderId(dto);
        List<ProductReviewVO> orderReviews = productReviewService.getOrderReviews(dto.getUserId(), orderId);

        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setOrderReviews(orderReviews);

        String reply = orderReviews.isEmpty()
                ? "该订单当前还没有评价记录。"
                : "已为你找到订单 " + orderId + " 的评价记录。";
        return buildResponse(sessionId, intent, reply, payload, List.of(
                "查询我的订单",
                "查看订单详情 " + orderId,
                "帮我推荐几款热销商品"));
    }

    private AssistantChatResponseVO handleSubmitReview(AssistantChatRequestDTO dto, String sessionId,
                                                       AssistantIntentEnum intent) {
        ReviewSubmitDTO reviewSubmitDTO = new ReviewSubmitDTO();
        reviewSubmitDTO.setUserId(dto.getUserId());
        reviewSubmitDTO.setOrderId(extractOrderId(dto));
        reviewSubmitDTO.setReviews(dto.getReviews());
        List<ProductReviewVO> submittedReviews = productReviewService.submitReview(reviewSubmitDTO);

        AssistantChatPayloadVO payload = new AssistantChatPayloadVO();
        payload.setOrderReviews(submittedReviews);

        AssistantOperationVO operationVO = new AssistantOperationVO();
        operationVO.setAction(intent.getCode());
        operationVO.setOrderId(reviewSubmitDTO.getOrderId());
        operationVO.setMessage("订单评价已提交");
        payload.setOperation(operationVO);

        return buildResponse(sessionId, intent,
                "订单 " + reviewSubmitDTO.getOrderId() + " 的评价已提交。",
                payload,
                List.of("查看订单评价 " + reviewSubmitDTO.getOrderId(), "查询我的订单", "帮我推荐几款热销商品"));
    }

    private AssistantChatResponseVO buildResponse(String sessionId, AssistantIntentEnum intent, String reply,
                                                  AssistantChatPayloadVO payload, List<String> suggestions) {
        AssistantChatResponseVO response = new AssistantChatResponseVO();
        response.setSessionId(sessionId);
        response.setIntentType(intent.getCode());
        response.setIntentDesc(intent.getDesc());
        response.setReply(reply);
        response.setPayload(payload);
        response.setSuggestions(filterSuggestions(suggestions));
        response.setResponseTime(new Date());
        return response;
    }

    private List<String> filterSuggestions(List<String> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return DEFAULT_SUGGESTIONS;
        }
        return suggestions.stream()
                .filter(StringTools::isNotEmpty)
                .distinct()
                .limit(3)
                .toList();
    }

    private AssistantIntentEnum resolveIntent(AssistantChatRequestDTO dto) {
        String message = dto.getMessage() == null ? "" : dto.getMessage().toLowerCase(Locale.ROOT);
        if (containsAny(message, "推荐", "热销", "爆款")) {
            return AssistantIntentEnum.PRODUCT_RECOMMEND;
        }
        if (containsAny(message, "申请退款", "发起退款")
                || (containsAny(message, "退款") && containsAny(message, "申请", "发起"))) {
            return AssistantIntentEnum.REFUND_APPLY;
        }
        if (containsAny(message, "退款详情", "退款记录", "查询退款")) {
            return AssistantIntentEnum.REFUND_DETAIL;
        }
        if (containsAny(message, "确认收货")) {
            return AssistantIntentEnum.RECEIVE_CONFIRM;
        }
        if (containsAny(message, "评价记录", "订单评价", "查看评价")) {
            return AssistantIntentEnum.ORDER_REVIEW_QUERY;
        }
        if (containsAny(message, "提交评价", "评价订单") || (dto.getReviews() != null && !dto.getReviews().isEmpty())) {
            return AssistantIntentEnum.REVIEW_SUBMIT;
        }
        if (((containsAny(message, "取消") && containsAny(message, "订单")) || StringTools.isNotEmpty(dto.getOrderId()))
                && containsAny(message, "取消", "撤销")) {
            return AssistantIntentEnum.ORDER_CANCEL;
        }
        if (containsAny(message, "订单详情", "查看订单", "订单明细")) {
            return AssistantIntentEnum.ORDER_DETAIL;
        }
        if (containsAny(message, "我的订单", "查询订单", "订单列表")) {
            return AssistantIntentEnum.ORDER_LIST;
        }
        if (containsAny(message, "详情", "介绍", "看看") && hasProductId(dto)) {
            return AssistantIntentEnum.PRODUCT_DETAIL;
        }
        if (hasOrderId(dto)) {
            return AssistantIntentEnum.ORDER_DETAIL;
        }
        if (hasProductId(dto)) {
            return AssistantIntentEnum.PRODUCT_DETAIL;
        }
        return AssistantIntentEnum.PRODUCT_SEARCH;
    }

    private Integer resolveOrderStatus(String message) {
        if (StringTools.isEmpty(message)) {
            return null;
        }
        if (message.contains("待支付")) {
            return 0;
        }
        if (message.contains("已支付")) {
            return 10;
        }
        if (message.contains("已取消")) {
            return 20;
        }
        if (message.contains("已完成")) {
            return 30;
        }
        if (message.contains("已发货")) {
            return 40;
        }
        if (message.contains("已收货")) {
            return 50;
        }
        if (message.contains("退款")) {
            return 60;
        }
        return null;
    }

    private String extractProductKeyword(String message) {
        if (StringTools.isEmpty(message)) {
            return "";
        }
        String keyword = PRODUCT_ID_PATTERN.matcher(message).replaceAll(" ");
        String[] tokens = {"帮我", "我想", "我要", "我想买", "查一下", "查找", "搜索", "搜", "商品", "推荐",
                "热销", "爆款", "详情", "介绍", "看看", "一下", "给我", "找", "买", "的"};
        for (String token : tokens) {
            keyword = keyword.replace(token, " ");
        }
        return keyword.replaceAll("[，。,.!?！？:：]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String extractProductId(AssistantChatRequestDTO dto) {
        if (StringTools.isNotEmpty(dto.getProductId())) {
            return dto.getProductId();
        }
        return extractByPattern(dto.getMessage(), PRODUCT_ID_PATTERN);
    }

    private String extractOrderId(AssistantChatRequestDTO dto) {
        if (StringTools.isNotEmpty(dto.getOrderId())) {
            return dto.getOrderId();
        }
        return extractByPattern(dto.getMessage(), ORDER_ID_PATTERN);
    }

    private String extractByPattern(String text, Pattern pattern) {
        if (StringTools.isEmpty(text)) {
            return "";
        }
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private boolean hasProductId(AssistantChatRequestDTO dto) {
        return StringTools.isNotEmpty(extractProductId(dto));
    }

    private boolean hasOrderId(AssistantChatRequestDTO dto) {
        return StringTools.isNotEmpty(extractOrderId(dto));
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeSessionId(String sessionId) {
        return StringTools.isEmpty(sessionId) ? StringTools.getRandomNumber(LENGTH_32) : sessionId;
    }

    private void saveChatLog(AssistantChatRequestDTO dto, AssistantChatResponseVO response) {
        AssistantChatLog chatLog = new AssistantChatLog();
        chatLog.setChatId(StringTools.getRandomNumber(LENGTH_32));
        chatLog.setSessionId(response.getSessionId());
        chatLog.setUserId(dto.getUserId());
        chatLog.setRequestText(dto.getMessage());
        chatLog.setIntentType(response.getIntentType());
        chatLog.setReplyText(response.getReply());
        chatLog.setPayloadSummary(truncate(buildPayloadSummary(response)));
        chatLog.setCreateTime(response.getResponseTime());
        assistantChatLogService.save(chatLog);
        log.info("assistant handled message, userId={}, sessionId={}, intent={}",
                dto.getUserId(), response.getSessionId(), response.getIntentType());
    }

    private String buildPayloadSummary(AssistantChatResponseVO response) {
        AssistantChatPayloadVO payload = response.getPayload();
        if (payload == null) {
            return response.getReply();
        }
        if (payload.getProductDetail() != null && payload.getProductDetail().getProductInfo() != null) {
            return "productDetail:" + payload.getProductDetail().getProductInfo().getProductId();
        }
        if (payload.getOrderDetail() != null) {
            return "orderDetail:" + payload.getOrderDetail().getOrderId();
        }
        if (payload.getRefundInfo() != null) {
            return "refundInfo:" + payload.getRefundInfo().getRefundId();
        }
        if (payload.getShippingInfo() != null) {
            return "shippingInfo:" + payload.getShippingInfo().getShippingId();
        }
        if (payload.getOrderReviews() != null) {
            return "orderReviews:" + payload.getOrderReviews().size();
        }
        if (payload.getOperation() != null) {
            return "operation:" + payload.getOperation().getAction() + ":" + payload.getOperation().getOrderId();
        }
        if (payload.getProductPage() != null) {
            return "productPage:" + payload.getProductPage().getTotalCount();
        }
        if (payload.getOrderPage() != null) {
            return "orderPage:" + payload.getOrderPage().getTotalCount();
        }
        return response.getReply();
    }

    private String truncate(String text) {
        if (text == null || text.length() <= MAX_SUMMARY_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_SUMMARY_LENGTH);
    }

    private AssistantChatHistoryVO buildHistoryVO(AssistantChatLog chatLog) {
        AssistantChatHistoryVO historyVO = new AssistantChatHistoryVO();
        BeanUtils.copyProperties(chatLog, historyVO);
        historyVO.setIntentDesc(AssistantIntentEnum.getByCode(chatLog.getIntentType()).getDesc());
        return historyVO;
    }

    private String firstProductId(List<ProductInfoListVO> products) {
        if (products == null || products.isEmpty() || StringTools.isEmpty(products.get(0).getProductId())) {
            return "p10001";
        }
        return products.get(0).getProductId();
    }

    private String firstOrderId(List<OrderInfoListVO> orders) {
        if (orders == null || orders.isEmpty() || StringTools.isEmpty(orders.get(0).getOrderId())) {
            return "o10001";
        }
        return orders.get(0).getOrderId();
    }

    private String firstCancelableOrderId(List<OrderInfoListVO> orders) {
        if (orders == null || orders.isEmpty()) {
            return "o10001";
        }
        return orders.stream()
                .filter(item -> item.getOrderStatus() != null && item.getOrderStatus() == 0)
                .map(OrderInfoListVO::getOrderId)
                .findFirst()
                .orElse(firstOrderId(orders));
    }
}
