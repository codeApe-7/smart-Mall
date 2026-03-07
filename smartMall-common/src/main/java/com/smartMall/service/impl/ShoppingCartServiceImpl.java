package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.ProductPropertyValue;
import com.smartMall.entities.domain.ProductSku;
import com.smartMall.entities.domain.ShoppingCart;
import com.smartMall.entities.dto.CartAddDTO;
import com.smartMall.entities.dto.CartDeleteDTO;
import com.smartMall.entities.dto.CartQuantityUpdateDTO;
import com.smartMall.entities.dto.CartSelectedUpdateDTO;
import com.smartMall.entities.enums.ProductStatusEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.ShoppingCartItemVO;
import com.smartMall.entities.vo.ShoppingCartVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.mapper.ShoppingCartMapper;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductPropertyValueService;
import com.smartMall.service.ProductSkuService;
import com.smartMall.service.ShoppingCartService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * 购物车 Service 实现。
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {

    private static final int SELECTED = 1;
    private static final int UNSELECTED = 0;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private ProductPropertyValueService productPropertyValueService;

    @Override
    public ShoppingCartVO loadCart(String userId) {
        validateUserId(userId);
        List<ShoppingCart> cartList = this.list(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getUpdateTime));
        if (cartList.isEmpty()) {
            return emptyCart();
        }

        Set<String> productIds = cartList.stream().map(ShoppingCart::getProductId).collect(Collectors.toSet());
        Map<String, ProductInfo> productMap = productInfoService.listByIds(productIds).stream()
                .collect(Collectors.toMap(ProductInfo::getProductId, product -> product, (left, right) -> left));

        Map<String, ProductSku> skuMap = productSkuService.list(new LambdaQueryWrapper<ProductSku>()
                        .in(ProductSku::getProductId, productIds))
                .stream()
                .collect(Collectors.toMap(
                        sku -> buildSkuKey(sku.getProductId(), sku.getPropertyValueIdHash()),
                        sku -> sku,
                        (left, right) -> left));

        Set<String> propertyValueIds = cartList.stream()
                .flatMap(cart -> splitPropertyValueIds(cart.getPropertyValueIds()).stream())
                .collect(Collectors.toSet());
        Map<String, ProductPropertyValue> propertyValueMap = loadPropertyValueMap(productIds, propertyValueIds);

        List<ShoppingCartItemVO> items = cartList.stream()
                .map(cart -> buildCartItem(cart, productMap, skuMap, propertyValueMap))
                .toList();

        ShoppingCartVO shoppingCartVO = new ShoppingCartVO();
        shoppingCartVO.setItems(items);
        shoppingCartVO.setItemCount(items.size());
        shoppingCartVO.setTotalQuantity(items.stream().mapToInt(ShoppingCartItemVO::getQuantity).sum());
        shoppingCartVO.setSelectedCount((int) items.stream().filter(item -> Boolean.TRUE.equals(item.getSelected())).count());
        shoppingCartVO.setSelectedAmount(items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getSelected()) && Boolean.TRUE.equals(item.getAvailable()))
                .map(ShoppingCartItemVO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return shoppingCartVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCartItem(CartAddDTO dto) {
        validateUserId(dto.getUserId());
        ProductInfo productInfo = getVisibleProduct(dto.getProductId());
        ProductSku productSku = getProductSku(dto.getProductId(), dto.getPropertyValueIdHash());
        ShoppingCart existingCart = this.getOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, dto.getUserId())
                .eq(ShoppingCart::getProductId, dto.getProductId())
                .eq(ShoppingCart::getPropertyValueIdHash, dto.getPropertyValueIdHash()));

        int targetQuantity = dto.getQuantity();
        Date now = new Date();
        if (existingCart != null) {
            targetQuantity += existingCart.getQuantity();
            validateStock(targetQuantity, productSku.getStock());
            existingCart.setQuantity(targetQuantity);
            existingCart.setSelected(SELECTED);
            existingCart.setPropertyValueIds(productSku.getPropertyValueIds());
            existingCart.setUpdateTime(now);
            this.updateById(existingCart);
            log.info("merge cart item, userId={}, productId={}, quantity={}", dto.getUserId(), dto.getProductId(), targetQuantity);
            return;
        }

        validateStock(targetQuantity, productSku.getStock());
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setCartId(StringTools.getRandomNumber(LENGTH_32));
        shoppingCart.setUserId(dto.getUserId());
        shoppingCart.setProductId(productInfo.getProductId());
        shoppingCart.setPropertyValueIdHash(productSku.getPropertyValueIdHash());
        shoppingCart.setPropertyValueIds(productSku.getPropertyValueIds());
        shoppingCart.setQuantity(targetQuantity);
        shoppingCart.setSelected(SELECTED);
        shoppingCart.setCreateTime(now);
        shoppingCart.setUpdateTime(now);
        this.save(shoppingCart);
        log.info("add cart item, userId={}, productId={}, quantity={}", dto.getUserId(), dto.getProductId(), targetQuantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(CartQuantityUpdateDTO dto) {
        validateUserId(dto.getUserId());
        ShoppingCart shoppingCart = getCartById(dto.getUserId(), dto.getCartId());
        ProductSku productSku = getProductSku(shoppingCart.getProductId(), shoppingCart.getPropertyValueIdHash());
        validateStock(dto.getQuantity(), productSku.getStock());
        shoppingCart.setQuantity(dto.getQuantity());
        shoppingCart.setUpdateTime(new Date());
        this.updateById(shoppingCart);
        log.info("update cart quantity, userId={}, cartId={}, quantity={}", dto.getUserId(), dto.getCartId(), dto.getQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSelected(CartSelectedUpdateDTO dto) {
        validateUserId(dto.getUserId());
        ShoppingCart shoppingCart = getCartById(dto.getUserId(), dto.getCartId());
        shoppingCart.setSelected(Boolean.TRUE.equals(dto.getSelected()) ? SELECTED : UNSELECTED);
        shoppingCart.setUpdateTime(new Date());
        this.updateById(shoppingCart);
        log.info("update cart selected, userId={}, cartId={}, selected={}", dto.getUserId(), dto.getCartId(), dto.getSelected());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItems(CartDeleteDTO dto) {
        validateUserId(dto.getUserId());
        this.remove(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, dto.getUserId())
                .in(ShoppingCart::getCartId, dto.getCartIds()));
        log.info("delete cart items, userId={}, count={}", dto.getUserId(), dto.getCartIds().size());
    }

    private ShoppingCartVO emptyCart() {
        ShoppingCartVO shoppingCartVO = new ShoppingCartVO();
        shoppingCartVO.setItems(List.of());
        shoppingCartVO.setItemCount(0);
        shoppingCartVO.setTotalQuantity(0);
        shoppingCartVO.setSelectedCount(0);
        shoppingCartVO.setSelectedAmount(BigDecimal.ZERO);
        return shoppingCartVO;
    }

    private Map<String, ProductPropertyValue> loadPropertyValueMap(Set<String> productIds, Set<String> propertyValueIds) {
        if (propertyValueIds.isEmpty()) {
            return Map.of();
        }
        return productPropertyValueService.list(new LambdaQueryWrapper<ProductPropertyValue>()
                        .in(ProductPropertyValue::getProductId, productIds)
                        .in(ProductPropertyValue::getPropertyValueId, propertyValueIds))
                .stream()
                .collect(Collectors.toMap(
                        propertyValue -> buildPropertyValueKey(propertyValue.getProductId(), propertyValue.getPropertyValueId()),
                        propertyValue -> propertyValue,
                        (left, right) -> left));
    }

    private ShoppingCartItemVO buildCartItem(ShoppingCart cart,
                                             Map<String, ProductInfo> productMap,
                                             Map<String, ProductSku> skuMap,
                                             Map<String, ProductPropertyValue> propertyValueMap) {
        ProductInfo productInfo = productMap.get(cart.getProductId());
        ProductSku productSku = skuMap.get(buildSkuKey(cart.getProductId(), cart.getPropertyValueIdHash()));

        ShoppingCartItemVO itemVO = new ShoppingCartItemVO();
        itemVO.setCartId(cart.getCartId());
        itemVO.setProductId(cart.getProductId());
        itemVO.setPropertyValueIdHash(cart.getPropertyValueIdHash());
        itemVO.setPropertyValueIds(cart.getPropertyValueIds());
        itemVO.setQuantity(cart.getQuantity());
        itemVO.setSelected(Objects.equals(cart.getSelected(), SELECTED));

        if (productInfo != null) {
            itemVO.setProductName(productInfo.getProductName());
            itemVO.setProductCover(productInfo.getCover());
        }
        if (productSku != null) {
            itemVO.setPrice(productSku.getPrice() == null ? BigDecimal.ZERO : productSku.getPrice());
            itemVO.setStock(productSku.getStock());
        } else {
            itemVO.setPrice(BigDecimal.ZERO);
            itemVO.setStock(0);
        }

        itemVO.setSkuPropertyText(buildSkuPropertyText(cart.getProductId(), cart.getPropertyValueIds(), propertyValueMap));
        itemVO.setAvailable(isAvailable(productInfo, productSku, cart.getQuantity()));
        itemVO.setTotalAmount(itemVO.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        return itemVO;
    }

    private String buildSkuPropertyText(String productId, String propertyValueIds,
                                        Map<String, ProductPropertyValue> propertyValueMap) {
        List<String> texts = splitPropertyValueIds(propertyValueIds).stream()
                .map(propertyValueId -> propertyValueMap.get(buildPropertyValueKey(productId, propertyValueId)))
                .filter(Objects::nonNull)
                .map(ProductPropertyValue::getPropertyValue)
                .toList();
        return texts.isEmpty() ? propertyValueIds : String.join(" / ", texts);
    }

    private boolean isAvailable(ProductInfo productInfo, ProductSku productSku, Integer quantity) {
        return productInfo != null
                && Objects.equals(productInfo.getStatus(), ProductStatusEnum.ON_SALE.getStatus())
                && productSku != null
                && productSku.getStock() != null
                && productSku.getStock() >= quantity;
    }

    private ProductInfo getVisibleProduct(String productId) {
        ProductInfo productInfo = productInfoService.getById(productId);
        if (productInfo == null || !Objects.equals(productInfo.getStatus(), ProductStatusEnum.ON_SALE.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "product is unavailable");
        }
        return productInfo;
    }

    private ProductSku getProductSku(String productId, String propertyValueIdHash) {
        ProductSku productSku = productSkuService.getOne(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getPropertyValueIdHash, propertyValueIdHash));
        if (productSku == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "product sku not found");
        }
        return productSku;
    }

    private ShoppingCart getCartById(String userId, String cartId) {
        ShoppingCart shoppingCart = this.getOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getCartId, cartId));
        if (shoppingCart == null) {
            throw new BusinessException(ResponseCodeEnum.DATA_NOT_EXIST, "cart item not found");
        }
        return shoppingCart;
    }

    private void validateStock(Integer quantity, Integer stock) {
        if (stock == null || quantity > stock) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "cart quantity exceeds stock");
        }
    }

    private void validateUserId(String userId) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException(ResponseCodeEnum.PARAM_ERROR, "userId is required");
        }
    }

    private String buildSkuKey(String productId, String propertyValueIdHash) {
        return productId + "_" + propertyValueIdHash;
    }

    private String buildPropertyValueKey(String productId, String propertyValueId) {
        return productId + "_" + propertyValueId;
    }

    private List<String> splitPropertyValueIds(String propertyValueIds) {
        if (StringTools.isEmpty(propertyValueIds)) {
            return List.of();
        }
        String[] valueIdArr = propertyValueIds.split(",");
        List<String> valueIds = new ArrayList<>(valueIdArr.length);
        for (String valueId : valueIdArr) {
            if (StringTools.isNotEmpty(valueId)) {
                valueIds.add(valueId.trim());
            }
        }
        return valueIds;
    }
}
