package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.ProductPropertyValue;
import com.smartMall.mapper.ProductPropertyValueMapper;
import com.smartMall.service.ProductPropertyValueService;
import org.springframework.stereotype.Service;

/**
 * @author 15712
 * @description 针对表【product_property_value(商品属性)】的数据库操作Service实现
 * @createDate 2026-02-13 16:20:58
 */
@Service
public class ProductPropertyValueServiceImpl extends ServiceImpl<ProductPropertyValueMapper, ProductPropertyValue>
        implements ProductPropertyValueService {

}
