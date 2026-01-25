package com.smartMall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartMall.entities.domain.SysCategory;
import com.smartMall.entities.dto.SysCategoryQueryDTO;
import com.smartMall.entities.dto.SysCategorySaveDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.entities.vo.SysCategoryVO;
import com.smartMall.service.SysCategoryService;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制器
 *
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/25
 */
@RestController
@RequestMapping("/category")
public class SysCategoryController {

    @Resource
    private SysCategoryService sysCategoryService;

    /**
     * 分页查询分类列表
     *
     * @param queryDTO 查询参数（包含tree参数控制是否返回树形结构）
     * @return 分页结果或树形列表
     */
    @GetMapping("/list")
    public ResponseVO<?> list(SysCategoryQueryDTO queryDTO) {
        // 如果需要树形结构，返回完整树形列表（不分页）
        if (Boolean.TRUE.equals(queryDTO.getTree())) {
            List<SysCategoryVO> treeList = sysCategoryService.queryList(queryDTO);
            return ResponseVO.success(treeList);
        }
        // 分页查询
        PageResultVO<SysCategoryVO> pageResult = sysCategoryService.queryPage(queryDTO);
        return ResponseVO.success(pageResult);
    }

    /**
     * 获取所有分类列表（不分页，支持树形结构）
     *
     * @param tree 是否返回树形结构，默认false
     * @return 分类列表
     */
    @GetMapping("/listAll")
    public ResponseVO<List<SysCategoryVO>> listAll(
            @RequestParam(required = false, defaultValue = "false") Boolean tree) {
        SysCategoryQueryDTO queryDTO = new SysCategoryQueryDTO();
        queryDTO.setTree(tree);
        List<SysCategoryVO> voList = sysCategoryService.queryList(queryDTO);
        return ResponseVO.success(voList);
    }

    /**
     * 根据ID获取分类详情
     *
     * @param categoryId 分类ID
     * @return 分类信息
     */
    @GetMapping("/get/{categoryId}")
    public ResponseVO<SysCategoryVO> getById(@PathVariable String categoryId) {
        SysCategory category = sysCategoryService.getById(categoryId);
        return ResponseVO.success(sysCategoryService.convertToVO(category));
    }

    /**
     * 新增分类（自动设置排序值）
     *
     * @param category 分类信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public ResponseVO<Void> add(@RequestBody @Valid SysCategorySaveDTO category) {
        sysCategoryService.addCategory(category);
        return ResponseVO.success();
    }

    /**
     * 更新分类
     *
     * @param category 分类信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ResponseVO<Void> update(@RequestBody @Valid SysCategory category) {
        sysCategoryService.updateById(category);
        return ResponseVO.success();
    }

    /**
     * 删除分类（级联删除所有子分类）
     *
     * @param categoryId 分类ID
     * @return 操作结果
     */
    @PostMapping("/delete/{categoryId}")
    public ResponseVO<Void> delete(@PathVariable String categoryId) {
        sysCategoryService.deleteCategory(categoryId);
        return ResponseVO.success();
    }

    /**
     * 批量新增分类（自动设置排序值）
     *
     * @param categoryList 分类列表
     * @return 操作结果
     */
    @PostMapping("/addBatch")
    public ResponseVO<Void> addBatch(@RequestBody @Valid List<SysCategory> categoryList) {
        sysCategoryService.addCategoryBatch(categoryList);
        return ResponseVO.success();
    }

    /**
     * 批量更新分类
     *
     * @param categoryList 分类列表
     * @return 操作结果
     */
    @PostMapping("/updateBatch")
    public ResponseVO<Void> updateBatch(@RequestBody @Valid List<SysCategory> categoryList) {
        sysCategoryService.updateBatchById(categoryList);
        return ResponseVO.success();
    }

    /**
     * 批量新增或更新分类（根据ID判断：有ID则更新，无ID则新增，新增时自动设置排序值）
     *
     * @param categoryList 分类列表
     * @return 操作结果
     */
    @PostMapping("/saveOrUpdateBatch")
    public ResponseVO<Void> saveOrUpdateBatch(@RequestBody @Valid List<SysCategory> categoryList) {
        // 分离新增和更新的数据
        List<SysCategory> toAdd = categoryList.stream()
                .filter(c -> StringTools.isEmpty(c.getCategoryId()))
                .collect(java.util.stream.Collectors.toList());
        List<SysCategory> toUpdate = categoryList.stream()
                .filter(c -> StringTools.isNotEmpty(c.getCategoryId()))
                .collect(java.util.stream.Collectors.toList());

        // 新增的使用addCategoryBatch（自动设置排序值）
        if (!toAdd.isEmpty()) {
            sysCategoryService.addCategoryBatch(toAdd);
        }
        // 更新的使用updateBatchById
        if (!toUpdate.isEmpty()) {
            sysCategoryService.updateBatchById(toUpdate);
        }
        return ResponseVO.success();
    }

    /**
     * 批量删除分类（级联删除所有子分类）
     *
     * @param categoryIds 分类ID列表
     * @return 操作结果
     */
    @PostMapping("/deleteBatch")
    public ResponseVO<Void> deleteBatch(@RequestBody List<String> categoryIds) {
        sysCategoryService.deleteCategoryBatch(categoryIds);
        return ResponseVO.success();
    }

    /**
     * 根据父级ID获取子分类列表
     *
     * @param pCategoryId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/children/{pCategoryId}")
    public ResponseVO<List<SysCategoryVO>> getChildren(@PathVariable String pCategoryId) {
        LambdaQueryWrapper<SysCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysCategory::getPCategoryId, pCategoryId)
                .orderByAsc(SysCategory::getSort);
        List<SysCategory> list = sysCategoryService.list(queryWrapper);
        return ResponseVO.success(sysCategoryService.convertToVOList(list));
    }

    /**
     * 获取树形分类（从指定父节点开始）
     *
     * @param pCategoryId 父分类ID，为空或"0"表示从根节点开始
     * @return 树形分类列表
     */
    @GetMapping("/tree")
    public ResponseVO<List<SysCategoryVO>> getTree(@RequestParam(required = false) String pCategoryId) {
        SysCategoryQueryDTO queryDTO = new SysCategoryQueryDTO();
        queryDTO.setTree(true);
        queryDTO.setPCategoryId(pCategoryId);
        List<SysCategoryVO> tree = sysCategoryService.queryList(queryDTO);
        return ResponseVO.success(tree);
    }

    /**
     * 拖拽排序 - 传入逗号分隔的ID字符串
     *
     * @param categoryIds 逗号分隔的分类ID字符串，按新的排序顺序排列
     * @return 操作结果
     */
    @PostMapping("/changeSort")
    public ResponseVO<Void> changeSort(@RequestParam String categoryIds) {
        sysCategoryService.changeSort(categoryIds);
        return ResponseVO.success();
    }

    /**
     * 拖拽排序 - 传入ID列表
     *
     * @param categoryIds 分类ID列表，按新的排序顺序排列
     * @return 操作结果
     */
    @PostMapping("/changeSortByList")
    public ResponseVO<Void> changeSortByList(@RequestBody List<String> categoryIds) {
        sysCategoryService.changeSortByList(categoryIds);
        return ResponseVO.success();
    }
}
