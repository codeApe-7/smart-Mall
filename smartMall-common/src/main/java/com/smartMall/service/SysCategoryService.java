package com.smartMall.service;

import com.smartMall.entities.domain.SysCategory;
import com.smartMall.entities.dto.SysCategoryQueryDTO;
import com.smartMall.entities.dto.SysCategorySaveDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.SysCategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author 15712
 * @description 针对表【sys_category】的数据库操作Service
 * @createDate 2026-01-25 14:11:01
 */
public interface SysCategoryService extends IService<SysCategory> {

    /**
     * 分页查询分类列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果VO
     */
    PageResultVO<SysCategoryVO> queryPage(SysCategoryQueryDTO queryDTO);

    /**
     * 查询分类列表（支持树形结构）
     *
     * @param queryDTO 查询参数
     * @return 分类VO列表（根据tree参数决定是否为树形结构）
     */
    List<SysCategoryVO> queryList(SysCategoryQueryDTO queryDTO);

    /**
     * 实体转VO
     *
     * @param category 分类实体
     * @return 分类VO
     */
    SysCategoryVO convertToVO(SysCategory category);

    /**
     * 实体列表转VO列表
     *
     * @param categoryList 分类实体列表
     * @return 分类VO列表
     */
    List<SysCategoryVO> convertToVOList(List<SysCategory> categoryList);

    /**
     * 构建树形结构（从根节点开始）
     *
     * @param voList 分类VO列表
     * @return 树形结构列表
     */
    List<SysCategoryVO> buildTree(List<SysCategoryVO> voList);

    /**
     * 构建树形结构（从指定父节点开始）
     *
     * @param voList      分类VO列表
     * @param pCategoryId 父分类ID
     * @return 树形结构列表
     */
    List<SysCategoryVO> buildTree(List<SysCategoryVO> voList, String pCategoryId);

    /**
     * 获取指定父分类下的最大排序值
     *
     * @param pCategoryId 父分类ID
     * @return 最大排序值，如果没有子分类则返回0
     */
    Integer getMaxSort(String pCategoryId);

    /**
     * 新增分类（自动设置sort值）
     *
     * @param category 分类实体
     */
    void addCategory(SysCategory category);

    /**
     * 新增分类（使用SaveDTO，自动设置sort值）
     *
     * @param saveDTO 分类保存DTO
     */
    void addCategory(SysCategorySaveDTO saveDTO);

    /**
     * 批量新增分类（自动设置sort值）
     *
     * @param categoryList 分类列表
     */
    void addCategoryBatch(List<SysCategory> categoryList);

    /**
     * 批量新增分类（使用SaveDTO，自动设置sort值）
     *
     * @param saveDTO 分类保存DTO列表
     */
    void addCategoryBatchDTO(List<SysCategorySaveDTO> saveDTOList);

    /**
     * SaveDTO转换为实体
     *
     * @param saveDTO 保存DTO
     * @return 分类实体
     */
    SysCategory convertToEntity(SysCategorySaveDTO saveDTO);

    /**
     * 删除分类（级联删除所有子分类）
     *
     * @param categoryId 分类ID
     */
    void deleteCategory(String categoryId);

    /**
     * 批量删除分类（级联删除所有子分类）
     *
     * @param categoryIds 分类ID列表
     */
    void deleteCategoryBatch(List<String> categoryIds);

    /**
     * 获取所有子分类ID（递归）
     *
     * @param categoryId 父分类ID
     * @return 所有子分类ID列表（包含自身）
     */
    List<String> getAllChildrenIds(String categoryId);

    /**
     * 拖拽排序 - 根据ID顺序批量更新排序值
     *
     * @param categoryIds 逗号分隔的分类ID字符串，按新的排序顺序排列
     */
    void changeSort(String categoryIds);

    /**
     * 拖拽排序 - 根据ID列表批量更新排序值
     *
     * @param categoryIdList 分类ID列表，按新的排序顺序排列
     */
    void changeSortByList(List<String> categoryIdList);
}
