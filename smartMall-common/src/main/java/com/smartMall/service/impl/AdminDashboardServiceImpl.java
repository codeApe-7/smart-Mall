package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.entities.config.AdminDashboardProperties;
import com.smartMall.entities.domain.OrderInfo;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.ProductSku;
import com.smartMall.entities.domain.RefundInfo;
import com.smartMall.entities.enums.OrderStatusEnum;
import com.smartMall.entities.enums.ProductStatusEnum;
import com.smartMall.entities.enums.RefundStatusEnum;
import com.smartMall.entities.vo.AdminDashboardOverviewVO;
import com.smartMall.entities.vo.AdminDashboardSummaryVO;
import com.smartMall.entities.vo.AdminDashboardTrendVO;
import com.smartMall.entities.vo.AdminLowStockProductVO;
import com.smartMall.entities.vo.AdminPendingShipmentOrderVO;
import com.smartMall.service.AdminDashboardService;
import com.smartMall.service.OrderInfoService;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductSkuService;
import com.smartMall.service.RefundInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Admin dashboard service implementation.
 */
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private AdminDashboardProperties adminDashboardProperties;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private RefundInfoService refundInfoService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductSkuService productSkuService;

    @Override
    public AdminDashboardOverviewVO getOverview() {
        List<OrderInfo> orderList = orderInfoService.list(new LambdaQueryWrapper<OrderInfo>()
                .orderByDesc(OrderInfo::getCreateTime));
        List<RefundInfo> refundList = refundInfoService.list(new LambdaQueryWrapper<RefundInfo>()
                .orderByDesc(RefundInfo::getCreateTime));

        AdminDashboardOverviewVO overviewVO = new AdminDashboardOverviewVO();
        overviewVO.setSummary(buildSummary(orderList, refundList));
        overviewVO.setSalesTrend(buildSalesTrend(orderList));
        overviewVO.setRefundTrend(buildRefundTrend(refundList));
        overviewVO.setPendingShipmentOrders(buildPendingShipmentOrders(orderList));
        overviewVO.setLowStockProducts(buildLowStockProducts());
        return overviewVO;
    }

    private AdminDashboardSummaryVO buildSummary(List<OrderInfo> orderList, List<RefundInfo> refundList) {
        AdminDashboardSummaryVO summaryVO = new AdminDashboardSummaryVO();
        List<OrderInfo> paidOrders = orderList.stream()
                .filter(this::isPaidOrder)
                .toList();
        List<RefundInfo> approvedRefunds = refundList.stream()
                .filter(item -> Objects.equals(item.getRefundStatus(), RefundStatusEnum.APPROVED.getStatus()))
                .toList();

        summaryVO.setTotalSalesAmount(sumAmount(paidOrders, OrderInfo::getTotalAmount));
        summaryVO.setTotalOrderCount((long) orderList.size());
        summaryVO.setTotalUserCount(orderList.stream()
                .map(OrderInfo::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count());
        summaryVO.setTotalRefundAmount(sumAmount(approvedRefunds, RefundInfo::getRefundAmount));
        summaryVO.setTotalRefundCount((long) refundList.size());
        summaryVO.setPendingRefundCount(refundList.stream()
                .filter(item -> Objects.equals(item.getRefundStatus(), RefundStatusEnum.PENDING.getStatus()))
                .count());
        summaryVO.setPendingShipmentCount(orderList.stream()
                .filter(item -> Objects.equals(item.getOrderStatus(), OrderStatusEnum.PAID.getStatus()))
                .count());
        return summaryVO;
    }

    private List<AdminDashboardTrendVO> buildSalesTrend(List<OrderInfo> orderList) {
        List<OrderInfo> paidOrders = orderList.stream()
                .filter(this::isPaidOrder)
                .filter(item -> item.getPayTime() != null)
                .toList();
        return buildTrend(paidOrders, OrderInfo::getPayTime, OrderInfo::getTotalAmount);
    }

    private List<AdminDashboardTrendVO> buildRefundTrend(List<RefundInfo> refundList) {
        return buildTrend(refundList, RefundInfo::getCreateTime, RefundInfo::getRefundAmount);
    }

    private <T> List<AdminDashboardTrendVO> buildTrend(List<T> sourceList,
                                                       Function<T, Date> dateGetter,
                                                       Function<T, BigDecimal> amountGetter) {
        int trendDays = Math.max(1, adminDashboardProperties.getTrendDays());
        LocalDate startDate = LocalDate.now().minusDays(trendDays - 1L);
        Map<LocalDate, List<T>> groupMap = sourceList.stream()
                .filter(item -> dateGetter.apply(item) != null)
                .filter(item -> !toLocalDate(dateGetter.apply(item)).isBefore(startDate))
                .collect(Collectors.groupingBy(item -> toLocalDate(dateGetter.apply(item))));

        List<AdminDashboardTrendVO> result = new ArrayList<>(trendDays);
        for (int i = 0; i < trendDays; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            List<T> items = groupMap.getOrDefault(currentDate, List.of());
            AdminDashboardTrendVO trendVO = new AdminDashboardTrendVO();
            trendVO.setDate(currentDate.format(DATE_FORMATTER));
            trendVO.setCount((long) items.size());
            trendVO.setAmount(items.stream()
                    .map(amountGetter)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(trendVO);
        }
        return result;
    }

    private List<AdminPendingShipmentOrderVO> buildPendingShipmentOrders(List<OrderInfo> orderList) {
        return orderList.stream()
                .filter(item -> Objects.equals(item.getOrderStatus(), OrderStatusEnum.PAID.getStatus()))
                .sorted(Comparator.comparing(OrderInfo::getPayTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(Math.max(1, adminDashboardProperties.getPendingShipmentLimit()))
                .map(this::buildPendingShipmentOrderVO)
                .toList();
    }

    private AdminPendingShipmentOrderVO buildPendingShipmentOrderVO(OrderInfo orderInfo) {
        AdminPendingShipmentOrderVO vo = new AdminPendingShipmentOrderVO();
        vo.setOrderId(orderInfo.getOrderId());
        vo.setOrderNo(orderInfo.getOrderNo());
        vo.setUserId(orderInfo.getUserId());
        vo.setReceiverName(orderInfo.getReceiverName());
        vo.setReceiverPhone(orderInfo.getReceiverPhone());
        vo.setTotalAmount(orderInfo.getTotalAmount());
        vo.setTotalQuantity(orderInfo.getTotalQuantity());
        vo.setPayTime(orderInfo.getPayTime());
        return vo;
    }

    private List<AdminLowStockProductVO> buildLowStockProducts() {
        List<ProductInfo> productList = productInfoService.list(new LambdaQueryWrapper<ProductInfo>()
                .eq(ProductInfo::getStatus, ProductStatusEnum.ON_SALE.getStatus()));
        if (productList.isEmpty()) {
            return List.of();
        }

        List<String> productIds = productList.stream().map(ProductInfo::getProductId).toList();
        List<ProductSku> skuList = productSkuService.list(new LambdaQueryWrapper<ProductSku>()
                .in(ProductSku::getProductId, productIds));
        Map<String, Integer> stockMap = skuList.stream()
                .collect(Collectors.groupingBy(ProductSku::getProductId,
                        LinkedHashMap::new,
                        Collectors.summingInt(item -> item.getStock() == null ? 0 : item.getStock())));

        int lowStockThreshold = Math.max(0, adminDashboardProperties.getLowStockThreshold());
        return productList.stream()
                .map(product -> buildLowStockProductVO(product, stockMap.getOrDefault(product.getProductId(), 0)))
                .filter(item -> item.getTotalStock() != null && item.getTotalStock() <= lowStockThreshold)
                .sorted(Comparator.comparing(AdminLowStockProductVO::getTotalStock)
                        .thenComparing(AdminLowStockProductVO::getTotalSale, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(Math.max(1, adminDashboardProperties.getLowStockLimit()))
                .toList();
    }

    private AdminLowStockProductVO buildLowStockProductVO(ProductInfo productInfo, Integer totalStock) {
        AdminLowStockProductVO vo = new AdminLowStockProductVO();
        vo.setProductId(productInfo.getProductId());
        vo.setProductName(productInfo.getProductName());
        vo.setCover(productInfo.getCover());
        vo.setMinPrice(productInfo.getMinPrice());
        vo.setTotalSale(productInfo.getTotalSale());
        vo.setTotalStock(totalStock);
        return vo;
    }

    private boolean isPaidOrder(OrderInfo orderInfo) {
        Set<Integer> paidStatusSet = Set.of(
                OrderStatusEnum.PAID.getStatus(),
                OrderStatusEnum.SHIPPED.getStatus(),
                OrderStatusEnum.RECEIVED.getStatus(),
                OrderStatusEnum.COMPLETED.getStatus(),
                OrderStatusEnum.REFUND_REQUESTED.getStatus(),
                OrderStatusEnum.REFUNDED.getStatus());
        return paidStatusSet.contains(orderInfo.getOrderStatus()) && orderInfo.getPayTime() != null;
    }

    private <T> BigDecimal sumAmount(List<T> sourceList, Function<T, BigDecimal> amountGetter) {
        return sourceList.stream()
                .map(amountGetter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
