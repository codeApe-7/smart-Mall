package com.smartMall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartMall.entities.domain.SysCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 15712
 * @description 针对表【sys_category】的数据库操作Mapper
 * @createDate 2026-01-25 14:11:01
 * @Entity generator.domain.SysCategory
 */
public interface SysCategoryMapper extends BaseMapper<SysCategory> {

    /**
     * 批量更新排序值
     *
     * @param categoryList 分类列表（只需要categoryId和sort字段）
     */
    void updateSortBatch(@Param("list") List<SysCategory> categoryList);
}
