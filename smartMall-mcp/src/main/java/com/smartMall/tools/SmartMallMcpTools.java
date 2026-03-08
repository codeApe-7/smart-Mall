package com.smartMall.tools;

import com.smartMall.entities.dto.ConfirmReceiveDTO;
import com.smartMall.entities.dto.OrderCancelDTO;
import com.smartMall.entities.dto.ProductKnowledgeCompareDTO;
import com.smartMall.entities.dto.ProductKnowledgeQueryDTO;
import com.smartMall.entities.dto.OrderQueryDTO;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.dto.RefundApplyDTO;
import com.smartMall.entities.vo.OrderDetailVO;
import com.smartMall.entities.vo.OrderInfoListVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.entities.vo.ProductKnowledgeCompareVO;
import com.smartMall.entities.vo.ProductKnowledgeVO;
import com.smartMall.entities.vo.ProductReviewVO;
import com.smartMall.entities.vo.RefundInfoVO;
import com.smartMall.entities.vo.ShippingInfoVO;
import com.smartMall.entities.vo.UserPreferenceVO;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductKnowledgeService;
import com.smartMall.service.ProductReviewService;
import com.smartMall.service.RefundInfoService;
import com.smartMall.service.ShippingInfoService;
import com.smartMall.service.UserPreferenceService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * SmartMall MCP tools.
 */
@Service
public class SmartMallMcpTools {

    private final ProductInfoService productInfoService;
    private final ProductKnowledgeService productKnowledgeService;
    private final OrderInfoService orderInfoService;
    private final RefundInfoService refundInfoService;
    private final ShippingInfoService shippingInfoService;
    private final ProductReviewService productReviewService;
    private final UserPreferenceService userPreferenceService;

    public SmartMallMcpTools(ProductInfoService productInfoService,
                             ProductKnowledgeService productKnowledgeService,
                             OrderInfoService orderInfoService,
                             RefundInfoService refundInfoService,
                             ShippingInfoService shippingInfoService,
                             ProductReviewService productReviewService,
                             UserPreferenceService userPreferenceService) {
        this.productInfoService = productInfoService;
        this.productKnowledgeService = productKnowledgeService;
        this.orderInfoService = orderInfoService;
        this.refundInfoService = refundInfoService;
        this.shippingInfoService = shippingInfoService;
        this.productReviewService = productReviewService;
        this.userPreferenceService = userPreferenceService;
    }

    public SmartMallMcpTools(ProductInfoService productInfoService,
                             OrderInfoService orderInfoService,
                             RefundInfoService refundInfoService,
                             ShippingInfoService shippingInfoService,
                             ProductReviewService productReviewService,
                             UserPreferenceService userPreferenceService) {
        this(productInfoService, null, orderInfoService, refundInfoService, shippingInfoService,
                productReviewService, userPreferenceService);
    }

    @Tool(name = "search_visible_products", description = "Search visible products by keyword")
    public PageResultVO<ProductInfoListVO> searchVisibleProducts(
            @ToolParam(description = "keyword for product search", required = true) String keyword,
            @ToolParam(description = "page size", required = false) Integer pageSize) {
        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setPageNo(1);
        queryDTO.setPageSize(pageSize == null || pageSize < 1 ? 5 : pageSize);
        queryDTO.setProductName(keyword);
        return productInfoService.loadVisibleProductList(queryDTO);
    }

    @Tool(name = "recommend_products", description = "Load recommended visible products")
    public List<ProductInfoListVO> recommendProducts(
            @ToolParam(description = "recommend product limit", required = false) Integer limit) {
        return productInfoService.loadRecommendProducts(limit);
    }

    @Tool(name = "get_product_detail", description = "Get visible product detail by productId")
    public ProductInfoDetailVo getProductDetail(
            @ToolParam(description = "product id", required = true) String productId) {
        return productInfoService.getVisibleProductDetail(productId);
    }

    @Tool(name = "search_product_knowledge", description = "Search product knowledge cards by keyword")
    public PageResultVO<ProductKnowledgeVO> searchProductKnowledge(
            @ToolParam(description = "keyword for product knowledge search", required = true) String keyword,
            @ToolParam(description = "page size", required = false) Integer pageSize) {
        ProductKnowledgeQueryDTO queryDTO = new ProductKnowledgeQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setPageNo(1);
        queryDTO.setPageSize(pageSize == null || pageSize < 1 ? 3 : pageSize);
        return productKnowledgeService.searchKnowledge(queryDTO);
    }

    @Tool(name = "get_product_knowledge", description = "Get product knowledge card by productId")
    public ProductKnowledgeVO getProductKnowledge(
            @ToolParam(description = "product id", required = true) String productId) {
        return productKnowledgeService.getKnowledgeDetail(productId);
    }

    @Tool(name = "compare_product_knowledge", description = "Compare multiple product knowledge cards")
    public ProductKnowledgeCompareVO compareProductKnowledge(
            @ToolParam(description = "keyword for compare search", required = false) String keyword,
            @ToolParam(description = "comma separated product ids", required = false) String productIds,
            @ToolParam(description = "max compare count", required = false) Integer maxCount) {
        ProductKnowledgeCompareDTO compareDTO = new ProductKnowledgeCompareDTO();
        compareDTO.setKeyword(keyword);
        compareDTO.setMaxCount(maxCount);
        compareDTO.setProductIds(parseProductIds(productIds));
        return productKnowledgeService.compareKnowledge(compareDTO);
    }

    @Tool(name = "list_orders", description = "List orders of one user")
    public PageResultVO<OrderInfoListVO> listOrders(
            @ToolParam(description = "user id", required = true) String userId,
            @ToolParam(description = "order status filter", required = false) Integer orderStatus,
            @ToolParam(description = "page size", required = false) Integer pageSize) {
        OrderQueryDTO queryDTO = new OrderQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setOrderStatus(orderStatus);
        queryDTO.setPageNo(1);
        queryDTO.setPageSize(pageSize == null || pageSize < 1 ? 5 : pageSize);
        return orderInfoService.loadOrderList(queryDTO);
    }

    @Tool(name = "get_order_detail", description = "Get order detail by userId and orderId")
    public OrderDetailVO getOrderDetail(
            @ToolParam(description = "user id", required = true) String userId,
            @ToolParam(description = "order id", required = true) String orderId) {
        return orderInfoService.getOrderDetail(userId, orderId);
    }

    @Tool(name = "cancel_order", description = "Cancel one pending payment order")
    public OrderDetailVO cancelOrder(
            @ToolParam(description = "user id", required = true) String userId,
            @ToolParam(description = "order id", required = true) String orderId) {
        OrderCancelDTO cancelDTO = new OrderCancelDTO();
        cancelDTO.setUserId(userId);
        cancelDTO.setOrderId(orderId);
        orderInfoService.cancelOrder(cancelDTO);
        return orderInfoService.getOrderDetail(userId, orderId);
    }

    @Tool(name = "apply_refund", description = "Apply refund for one order")
    public RefundInfoVO applyRefund(
            @ToolParam(description = "user id", required = true) String userId,
            @ToolParam(description = "order id", required = true) String orderId,
            @ToolParam(description = "refund reason", required = false) String refundReason) {
        RefundApplyDTO refundApplyDTO = new RefundApplyDTO();
        refundApplyDTO.setUserId(userId);
        refundApplyDTO.setOrderId(orderId);
        refundApplyDTO.setRefundReason(refundReason);
        return refundInfoService.applyRefund(refundApplyDTO);
    }

    @Tool(name = "get_refund_detail", description = "Get refund detail for one order")
    public RefundInfoVO getRefundDetail(
            @ToolParam(description = "user id", required = true) String userId,
            @ToolParam(description = "order id", required = true) String orderId) {
        return refundInfoService.getRefundDetail(userId, orderId);
    }

    @Tool(name = "confirm_receive", description = "Confirm receiving one shipped order")
    public ShippingInfoVO confirmReceive(
            @ToolParam(description = "user id", required = true) String userId,
            @ToolParam(description = "order id", required = true) String orderId) {
        ConfirmReceiveDTO confirmReceiveDTO = new ConfirmReceiveDTO();
        confirmReceiveDTO.setUserId(userId);
        confirmReceiveDTO.setOrderId(orderId);
        shippingInfoService.confirmReceive(confirmReceiveDTO);
        return shippingInfoService.getShippingDetail(userId, orderId);
    }

    @Tool(name = "get_order_reviews", description = "Get all reviews under one order")
    public List<ProductReviewVO> getOrderReviews(
            @ToolParam(description = "user id", required = true) String userId,
            @ToolParam(description = "order id", required = true) String orderId) {
        return productReviewService.getOrderReviews(userId, orderId);
    }

    @Tool(name = "get_user_preference", description = "Get user preference profile including favorite categories, price range, search history and tags")
    public UserPreferenceVO getUserPreference(
            @ToolParam(description = "user id", required = true) String userId) {
        return userPreferenceService.getUserPreference(userId);
    }

    @Tool(name = "personalized_recommend", description = "Recommend products based on user preference profile")
    public List<ProductInfoListVO> personalizedRecommend(
            @ToolParam(description = "user id", required = true) String userId,
            @ToolParam(description = "recommend product limit", required = false) Integer limit) {
        return productInfoService.loadPersonalizedRecommendProducts(userId, limit);
    }

    private List<String> parseProductIds(String productIds) {
        if (productIds == null || productIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(productIds.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
