package com.smartMall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.SysProductProperty;
import com.smartMall.entities.dto.SysProductPropertySaveDTO;
import com.smartMall.entities.vo.SysProductPropertyVO;
import com.smartMall.service.SysProductPropertyService;
import com.smartMall.mapper.SysProductPropertyMapper;
import com.smartMall.utils.StringTools;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author 15712
 * @description 针对表【sys_product_property(商品属性表)】的数据库操作Service实现
 * @createDate 2026-01-25 16:34:49
 */
@Service
public class SysProductPropertyServiceImpl extends ServiceImpl<SysProductPropertyMapper, SysProductProperty>
        implements SysProductPropertyService {

    @Override
    public List<SysProductPropertyVO> listByCategoryId(String categoryId) {
        return this.baseMapper.selectByCategoryId(categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProperty(SysProductPropertySaveDTO saveDTO) {
        SysProductProperty property = new SysProductProperty();
        BeanUtils.copyProperties(saveDTO, property);

        // 生成属性ID
        property.setPropertyId(UUID.randomUUID().toString().replace("-", ""));

        // 自动设置排序值（当前分类下最大排序值 + 1）
        Integer maxSort = this.baseMapper.getMaxSortByCategoryId(saveDTO.getCategoryId());
        property.setPropertySort(maxSort + 1);

        this.save(property);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProperty(SysProductProperty property) {
        this.updateById(property);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProperty(String propertyId) {
        if (StringTools.isNotEmpty(propertyId)) {
            this.removeById(propertyId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<String> propertyIds) {
        if (propertyIds != null && !propertyIds.isEmpty()) {
            this.removeByIds(propertyIds);
        }
    }
}
