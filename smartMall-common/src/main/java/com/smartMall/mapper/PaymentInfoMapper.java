package com.smartMall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartMall.entities.domain.PaymentInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * Payment mapper.
 */
@Mapper
public interface PaymentInfoMapper extends BaseMapper<PaymentInfo> {
}
