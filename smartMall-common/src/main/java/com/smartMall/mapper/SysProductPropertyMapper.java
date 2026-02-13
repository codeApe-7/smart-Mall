package com.smartMall.mapper;

import com.smartMall.entities.domain.SysProductProperty;
import com.smartMall.entities.vo.SysProductPropertyVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 15712
 * @description 针对表【sys_product_property(商品属性表)】的数据库操作Mapper
 * @createDate 2026-01-25 16:34:49
 * @Entity com.smartMall.entities.domain.SysProductProperty
 */
public interface SysProductPropertyMapper extends BaseMapper<SysProductProperty> {

    /**
     * 联表查询：根据二级分类ID查询属性列表（带分类名称）
     */
    List<SysProductPropertyVO> selectByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 联表查询：根据属性ID获取属性详情（带分类名称）
     */
    SysProductPropertyVO selectVOById(@Param("propertyId") String propertyId);

    /**
     * 获取指定分类下的最大排序值
     */
    Integer getMaxSortByCategoryId(@Param("categoryId") String categoryId);
}
