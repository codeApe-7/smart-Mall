package com.smartMall.service;

import com.smartMall.entities.domain.SysProductProperty;
import com.smartMall.entities.dto.SysProductPropertySaveDTO;
import com.smartMall.entities.vo.SysProductPropertyVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author 15712
 * @description 针对表【sys_product_property(商品属性表)】的数据库操作Service
 * @createDate 2026-01-25 16:34:49
 */
public interface SysProductPropertyService extends IService<SysProductProperty> {

    /**
     * 根据二级分类ID查询属性列表（联表查询，带分类名称）
     *
     * @param categoryId 二级分类ID
     * @return 属性VO列表
     */
    List<SysProductPropertyVO> listByCategoryId(String categoryId);

    /**
     * 新增属性（自动设置排序值）
     *
     * @param saveDTO 属性保存DTO
     */
    void addProperty(SysProductPropertySaveDTO saveDTO);

    /**
     * 更新属性
     *
     * @param property 属性实体
     */
    void updateProperty(SysProductProperty property);

    /**
     * 删除属性
     *
     * @param propertyId 属性ID
     */
    void deleteProperty(String propertyId);

    /**
     * 批量删除属性
     *
     * @param propertyIds 属性ID列表
     */
    void deleteBatch(List<String> propertyIds);
}
