package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.domain.ProductPropertyValue;
import com.smartMall.entities.domain.ProductSku;
import com.smartMall.entities.domain.SysCategory;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.dto.ProductSaveDTO;
import com.smartMall.entities.enums.ProductStatusEnum;
import com.smartMall.entities.vo.*;
import com.smartMall.mapper.ProductInfoMapper;
import com.smartMall.service.ProductInfoService;
import com.smartMall.service.ProductPropertyValueService;
import com.smartMall.service.ProductSkuService;
import com.smartMall.service.SysCategoryService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.smartMall.entities.constant.Constants.LENGTH_32;

/**
 * @author 15712
 * @description 针对表【product_info(商品信息)】的数据库操作Service实现
 * @createDate 2026-02-13 15:52:46
 */
@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfo>
        implements ProductInfoService {

    @Resource
    private ProductPropertyValueService productPropertyValueService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private SysCategoryService sysCategoryService;

    @Override
    public PageResultVO<ProductInfoListVO> loadProductList(ProductQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<ProductInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringTools.isNotEmpty(queryDTO.getProductName()),
                        ProductInfo::getProductName, queryDTO.getProductName())
                .and(StringTools.isNotEmpty(queryDTO.getCategoryIdOrPCategoryId()),
                        w -> w.eq(ProductInfo::getCategoryId, queryDTO.getCategoryIdOrPCategoryId())
                                .or()
                                .eq(ProductInfo::getPCategoryId, queryDTO.getCategoryIdOrPCategoryId()))
                .eq(queryDTO.getCommendType() != null,
                        ProductInfo::getCommendType, queryDTO.getCommendType())
                .orderByDesc(ProductInfo::getCreateTime);

        // 2. 分页查询
        Page<ProductInfo> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        this.page(page, queryWrapper);

        List<ProductInfo> productList = page.getRecords();
        if (productList.isEmpty()) {
            return PageResultVO.empty(queryDTO.getPageNo(), queryDTO.getPageSize());
        }

        // 3. 批量查询分类名称
        Set<String> categoryIds = productList.stream()
                .map(ProductInfo::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<SysCategory> categories = sysCategoryService.listByIds(categoryIds);
            categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(SysCategory::getCategoryId, SysCategory::getCategoryName, (a, b) -> a));
        }

        // 4. 批量查询 SKU（按 productId 分组）
        List<String> productIds = productList.stream()
                .map(ProductInfo::getProductId)
                .collect(Collectors.toList());
        List<ProductSku> allSkus = productSkuService.list(
                new LambdaQueryWrapper<ProductSku>().in(ProductSku::getProductId, productIds));
        Map<String, List<ProductSku>> skuGroupMap = allSkus.stream()
                .collect(Collectors.groupingBy(ProductSku::getProductId));

        // 5. 组装 VO
        Map<String, String> finalCategoryNameMap = categoryNameMap;
        List<ProductInfoListVO> voList = productList.stream().map(product -> {
            ProductInfoListVO vo = new ProductInfoListVO();
            BeanUtils.copyProperties(product, vo);
            vo.setCategoryName(finalCategoryNameMap.getOrDefault(product.getCategoryId(), ""));

            List<ProductSku> skus = skuGroupMap.getOrDefault(product.getProductId(), List.of());
            vo.setSkuCount(skus.size());
            vo.setTotalStock(skus.stream()
                    .mapToInt(s -> s.getStock() != null ? s.getStock() : 0)
                    .sum());
            return vo;
        }).collect(Collectors.toList());

        return new PageResultVO<>(queryDTO.getPageNo(), queryDTO.getPageSize(), page.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(ProductSaveDTO productSaveDTO) {
        // 1. 从DTO中提取数据并转换为实体
        ProductInfoVO productInfoVO = productSaveDTO.getProductInfo();
        ProductInfo productInfo = new ProductInfo();
        BeanUtils.copyProperties(productInfoVO, productInfo);

        List<ProductPropertyValue> productPropertyValueList = convertPropertyValues(
                productSaveDTO.getProductPropertyList());
        List<ProductSku> skuList = convertSkus(productSaveDTO.getSkuList());

        // 2. 判断是新增还是更新
        boolean isAdd = StringTools.isEmpty(productInfo.getProductId());
        if (isAdd) {
            productInfo.setProductId(StringTools.getRandomNumber(LENGTH_32));
        }

        // 3. 为属性值和SKU设置商品ID
        productPropertyValueList.forEach(p -> p.setProductId(productInfo.getProductId()));
        skuList.forEach(s -> s.setProductId(productInfo.getProductId()));

        // 4. 清除服务端管理的字段（防止前端篡改）
        productInfo.setStatus(null);
        productInfo.setCommendType(null);

        // 5. 计算最低/最高价格
        Optional<ProductSku> minPrice = skuList.stream()
                .min((s1, s2) -> s1.getPrice().compareTo(s2.getPrice()));
        Optional<ProductSku> maxPrice = skuList.stream()
                .max((s1, s2) -> s1.getPrice().compareTo(s2.getPrice()));
        productInfo.setMinPrice(minPrice.get().getPrice());
        productInfo.setMaxPrice(maxPrice.get().getPrice());

        // 6. 执行数据库操作
        if (isAdd) {
            productInfo.setCreateTime(new Date());
            productInfo.setStatus(ProductStatusEnum.OFF_SALE.getStatus());

            this.baseMapper.insert(productInfo);
            productPropertyValueService.saveBatch(productPropertyValueList);
            productSkuService.saveBatch(skuList);
        } else {
            // 更新：先删除旧的属性值和SKU，再重新插入
            this.baseMapper.updateById(productInfo);

            productPropertyValueService.remove(
                    new LambdaQueryWrapper<ProductPropertyValue>()
                            .eq(ProductPropertyValue::getProductId, productInfo.getProductId()));
            productSkuService.remove(
                    new LambdaQueryWrapper<ProductSku>()
                            .eq(ProductSku::getProductId, productInfo.getProductId()));

            if (!productPropertyValueList.isEmpty()) {
                productPropertyValueService.saveBatch(productPropertyValueList);
            }
            if (!skuList.isEmpty()) {
                productSkuService.saveBatch(skuList);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(String productId) {
        // 删除商品信息
        this.baseMapper.deleteById(productId);

        // 级联删除属性值
        productPropertyValueService.remove(
                new LambdaQueryWrapper<ProductPropertyValue>()
                        .eq(ProductPropertyValue::getProductId, productId));

        // 级联删除SKU
        productSkuService.remove(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId));
    }

    /**
     * 将属性值VO列表转换为实体列表
     */
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

    /**
     * 将SKU VO列表转换为实体列表
     */
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
