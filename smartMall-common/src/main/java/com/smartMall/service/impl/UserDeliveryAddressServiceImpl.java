package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.UserDeliveryAddress;
import com.smartMall.mapper.UserDeliveryAddressMapper;
import com.smartMall.service.UserDeliveryAddressService;
import org.springframework.stereotype.Service;

/**
 * 用户收货地址 Service 实现。
 */
@Service
public class UserDeliveryAddressServiceImpl extends ServiceImpl<UserDeliveryAddressMapper, UserDeliveryAddress>
        implements UserDeliveryAddressService {
}
