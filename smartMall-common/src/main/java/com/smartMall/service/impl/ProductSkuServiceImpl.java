package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.ProductSku;
import com.smartMall.mapper.ProductSkuMapper;
import com.smartMall.service.ProductSkuService;
import org.springframework.stereotype.Service;

/**
 * @author 15712
 * @description 针对表【product_sku(商品SKU)】的数据库操作Service实现
 * @createDate 2026-02-13 16:20:58
 */
@Service
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku>
        implements ProductSkuService {

}
