package com.smartMall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartMall.entities.domain.SysCategory;
import com.smartMall.entities.dto.SysCategoryQueryDTO;
import com.smartMall.entities.dto.SysCategorySaveDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.SysCategoryVO;
import com.smartMall.mapper.SysCategoryMapper;
import com.smartMall.service.SysCategoryService;
import com.smartMall.utils.StringTools;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 15712
 * @description 针对表【sys_category】的数据库操作Service实现
 * @createDate 2026-01-25 14:11:01
 */
@Service
public class SysCategoryServiceImpl extends ServiceImpl<SysCategoryMapper, SysCategory>
                implements SysCategoryService {

        @Override
        public PageResultVO<SysCategoryVO> queryPage(SysCategoryQueryDTO queryDTO) {
                // 构建分页对象
                Page<SysCategory> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());

                // 构建查询条件
                LambdaQueryWrapper<SysCategory> queryWrapper = buildQueryWrapper(queryDTO);

                // 执行分页查询
                IPage<SysCategory> pageResult = this.page(page, queryWrapper);

                // 转换为VO
                List<SysCategoryVO> voList = convertToVOList(pageResult.getRecords());

                // 构建分页结果
                PageResultVO<SysCategoryVO> resultVO = new PageResultVO<>();
                resultVO.setPageNo((int) pageResult.getCurrent());
                resultVO.setPageSize((int) pageResult.getSize());
                resultVO.setTotalCount(pageResult.getTotal());
                resultVO.setTotalPages((int) pageResult.getPages());
                resultVO.setRecords(voList);

                return resultVO;
        }

        @Override
        public List<SysCategoryVO> queryList(SysCategoryQueryDTO queryDTO) {
                // 构建查询条件
                LambdaQueryWrapper<SysCategory> queryWrapper = buildQueryWrapper(queryDTO);

                // 执行查询
                List<SysCategory> list = this.list(queryWrapper);

                // 转换为VO
                List<SysCategoryVO> voList = convertToVOList(list);

                // 如果需要树形结构
                if (Boolean.TRUE.equals(queryDTO.getTree())) {
                        voList = buildTree(voList, queryDTO.getPCategoryId());
                }

                return voList;
        }

        @Override
        public SysCategoryVO convertToVO(SysCategory category) {
                if (category == null) {
                        return null;
                }
                SysCategoryVO vo = new SysCategoryVO();
                BeanUtils.copyProperties(category, vo);
                return vo;
        }

        @Override
        public List<SysCategoryVO> convertToVOList(List<SysCategory> categoryList) {
                if (categoryList == null || categoryList.isEmpty()) {
                        return new ArrayList<>();
                }
                return categoryList.stream()
                                .map(this::convertToVO)
                                .collect(Collectors.toList());
        }

        @Override
        public List<SysCategoryVO> buildTree(List<SysCategoryVO> voList) {
                return buildTree(voList, null);
        }

        @Override
        public List<SysCategoryVO> buildTree(List<SysCategoryVO> voList, String pCategoryId) {
                if (voList == null || voList.isEmpty()) {
                        return new ArrayList<>();
                }

                // 按父ID分组（-1表示根节点）
                Map<String, List<SysCategoryVO>> parentIdMap = voList.stream()
                                .collect(Collectors.groupingBy(
                                                vo -> vo.getPCategoryId() == null ? "-1" : vo.getPCategoryId()));

                // 递归设置子节点
                voList.forEach(vo -> {
                        List<SysCategoryVO> children = parentIdMap.get(vo.getCategoryId());
                        if (children != null && !children.isEmpty()) {
                                vo.setChildren(children);
                        }
                });

                // 获取根节点（父ID为空或"-1"表示根节点）
                String rootParentId = StringTools.isEmpty(pCategoryId) || "-1".equals(pCategoryId) ? "-1" : pCategoryId;

                // 返回顶层节点
                return voList.stream()
                                .filter(vo -> {
                                        String pid = vo.getPCategoryId();
                                        if ("-1".equals(rootParentId)) {
                                                // 如果从根开始，返回父ID为空或"-1"的节点
                                                return StringTools.isEmpty(pid) || "-1".equals(pid);
                                        } else {
                                                // 返回父ID等于指定值的节点
                                                return rootParentId.equals(pid);
                                        }
                                })
                                .collect(Collectors.toList());
        }

        /**
         * 构建查询条件
         */
        private LambdaQueryWrapper<SysCategory> buildQueryWrapper(SysCategoryQueryDTO queryDTO) {
                LambdaQueryWrapper<SysCategory> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.like(StringTools.isNotEmpty(queryDTO.getCategoryName()),
                                SysCategory::getCategoryName, queryDTO.getCategoryName())
                                .eq(StringTools.isNotEmpty(queryDTO.getPCategoryId()),
                                                SysCategory::getPCategoryId, queryDTO.getPCategoryId())
                                .orderByAsc(SysCategory::getSort);
                return queryWrapper;
        }

        @Override
        public Integer getMaxSort(String pCategoryId) {
                LambdaQueryWrapper<SysCategory> queryWrapper = new LambdaQueryWrapper<>();
                // 根据父分类ID查询（-1表示根节点）
                if (StringTools.isEmpty(pCategoryId) || "-1".equals(pCategoryId)) {
                        queryWrapper.and(wrapper -> wrapper.isNull(SysCategory::getPCategoryId)
                                        .or().eq(SysCategory::getPCategoryId, "")
                                        .or().eq(SysCategory::getPCategoryId, "-1"));
                } else {
                        queryWrapper.eq(SysCategory::getPCategoryId, pCategoryId);
                }
                queryWrapper.orderByDesc(SysCategory::getSort).last("LIMIT 1");

                SysCategory category = this.getOne(queryWrapper);
                return category != null && category.getSort() != null ? category.getSort() : 0;
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void addCategory(SysCategory category) {
                // 生成分类ID
                if (StringTools.isEmpty(category.getCategoryId())) {
                        category.setCategoryId(java.util.UUID.randomUUID().toString().replace("-", ""));
                }
                // 设置排序值（当前最大值 + 1）
                if (category.getSort() == null) {
                        Integer maxSort = getMaxSort(category.getPCategoryId());
                        category.setSort(maxSort + 1);
                }
                this.save(category);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void addCategoryBatch(List<SysCategory> categoryList) {
                if (categoryList == null || categoryList.isEmpty()) {
                        return;
                }
                // 按父分类ID分组，以便批量获取最大排序值（-1表示根节点）
                Map<String, List<SysCategory>> groupByParent = categoryList.stream()
                                .collect(Collectors
                                                .groupingBy(c -> c.getPCategoryId() == null ? "-1"
                                                                : c.getPCategoryId()));

                for (Map.Entry<String, List<SysCategory>> entry : groupByParent.entrySet()) {
                        String pCategoryId = entry.getKey();
                        List<SysCategory> categories = entry.getValue();

                        // 获取当前父分类下的最大排序值
                        Integer maxSort = getMaxSort(pCategoryId);

                        for (SysCategory category : categories) {
                                // 生成分类ID
                                if (StringTools.isEmpty(category.getCategoryId())) {
                                        category.setCategoryId(java.util.UUID.randomUUID().toString().replace("-", ""));
                                }
                                // 设置排序值
                                if (category.getSort() == null) {
                                        maxSort++;
                                        category.setSort(maxSort);
                                }
                        }
                }
                this.saveBatch(categoryList);
        }

        @Override
        public SysCategory convertToEntity(SysCategorySaveDTO saveDTO) {
                if (saveDTO == null) {
                        return null;
                }
                SysCategory category = new SysCategory();
                BeanUtils.copyProperties(saveDTO, category);
                return category;
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void addCategory(SysCategorySaveDTO saveDTO) {
                SysCategory category = convertToEntity(saveDTO);
                addCategory(category);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void addCategoryBatchDTO(List<SysCategorySaveDTO> saveDTOList) {
                if (saveDTOList == null || saveDTOList.isEmpty()) {
                        return;
                }
                List<SysCategory> categoryList = saveDTOList.stream()
                                .map(this::convertToEntity)
                                .collect(Collectors.toList());
                addCategoryBatch(categoryList);
        }

        @Override
        public List<String> getAllChildrenIds(String categoryId) {
                List<String> allIds = new ArrayList<>();
                if (StringTools.isEmpty(categoryId)) {
                        return allIds;
                }
                // 添加当前分类ID
                allIds.add(categoryId);
                // 递归查询所有子分类
                collectChildrenIds(categoryId, allIds);
                return allIds;
        }

        /**
         * 递归收集所有子分类ID
         */
        private void collectChildrenIds(String parentId, List<String> allIds) {
                LambdaQueryWrapper<SysCategory> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysCategory::getPCategoryId, parentId);
                List<SysCategory> children = this.list(queryWrapper);

                if (children != null && !children.isEmpty()) {
                        for (SysCategory child : children) {
                                allIds.add(child.getCategoryId());
                                // 递归查询子分类的子分类
                                collectChildrenIds(child.getCategoryId(), allIds);
                        }
                }
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void deleteCategory(String categoryId) {
                if (StringTools.isEmpty(categoryId)) {
                        return;
                }
                // 获取所有需要删除的分类ID（包含自身和所有子分类）
                List<String> allIds = getAllChildrenIds(categoryId);
                if (!allIds.isEmpty()) {
                        this.removeByIds(allIds);
                }
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void deleteCategoryBatch(List<String> categoryIds) {
                if (categoryIds == null || categoryIds.isEmpty()) {
                        return;
                }
                // 收集所有需要删除的分类ID
                List<String> allIds = new ArrayList<>();
                for (String categoryId : categoryIds) {
                        List<String> childrenIds = getAllChildrenIds(categoryId);
                        allIds.addAll(childrenIds);
                }
                // 去重后删除
                if (!allIds.isEmpty()) {
                        List<String> distinctIds = allIds.stream().distinct().collect(Collectors.toList());
                        this.removeByIds(distinctIds);
                }
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void changeSort(String categoryIds) {
                if (StringTools.isEmpty(categoryIds)) {
                        return;
                }
                // 按逗号分隔解析ID
                String[] categoryIdArr = categoryIds.split(",");
                List<String> idList = java.util.Arrays.asList(categoryIdArr);
                changeSortByList(idList);
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public void changeSortByList(List<String> categoryIdList) {
                if (categoryIdList == null || categoryIdList.isEmpty()) {
                        return;
                }
                // 构建更新列表，sort从1开始递增
                List<SysCategory> updateList = new ArrayList<>();
                int sort = 1;
                for (String categoryId : categoryIdList) {
                        if (StringTools.isNotEmpty(categoryId)) {
                                SysCategory category = new SysCategory();
                                category.setCategoryId(categoryId.trim());
                                category.setSort(sort++);
                                updateList.add(category);
                        }
                }
                // 批量更新排序值
                if (!updateList.isEmpty()) {
                        this.baseMapper.updateSortBatch(updateList);
                }
        }
}
