package com.smartMall.controller;

import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.dto.ProductSaveDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.ProductInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器
 *
 * @author 15712
 * @date 2026/2/13
 */
@RestController
@RequestMapping("/product")
public class ProductInfoController {

    @Resource
    private ProductInfoService productInfoService;

    /**
     * 分页查询商品列表（含分类名称、SKU数量、总库存）
     *
     * @param productQueryDTO 查询参数
     * @return 分页结果
     */
    @PostMapping("/loadProductList")
    public ResponseVO<PageResultVO<ProductInfoListVO>> loadProductList(@RequestBody ProductQueryDTO productQueryDTO) {
        return ResponseVO.success(productInfoService.loadProductList(productQueryDTO));
    }

    /**
     * 新增/更新商品（根据productId判断：无ID则新增，有ID则更新）
     *
     * @param productSaveDTO 商品保存DTO（包含商品信息、属性值列表、SKU列表）
     * @return 操作结果
     */
    @PostMapping("/save")
    public ResponseVO<Void> saveProduct(@RequestBody @Valid ProductSaveDTO productSaveDTO) {
        productInfoService.saveProduct(productSaveDTO);
        return ResponseVO.success();
    }

    /**
     * 删除商品（级联删除属性值和SKU）
     *
     * @param productId 商品ID
     * @return 操作结果
     */
    @PostMapping("/delete/{productId}")
    public ResponseVO<Void> deleteProduct(@PathVariable String productId) {
        productInfoService.deleteProduct(productId);
        return ResponseVO.success();
    }
}
