package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.OrderItem;
import com.smartMall.mapper.OrderItemMapper;
import com.smartMall.service.OrderItemService;
import org.springframework.stereotype.Service;

/**
 * 订单明细 Service 实现。
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
        implements OrderItemService {
}
