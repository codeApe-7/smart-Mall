package com.smartMall.service;

import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.dto.ProductSaveDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 15712
 * @description 针对表【product_info(商品信息)】的数据库操作Service
 * @createDate 2026-02-13 15:52:46
 */
public interface ProductInfoService extends IService<ProductInfo> {

    /**
     * 分页查询商品列表（含分类名称、SKU数量、总库存）
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResultVO<ProductInfoListVO> loadProductList(ProductQueryDTO queryDTO);

    /**
     * 保存商品（新增或更新，根据productId判断）
     *
     * @param productSaveDTO 商品保存DTO
     */
    void saveProduct(ProductSaveDTO productSaveDTO);

    /**
     * 删除商品（级联删除属性值和SKU）
     *
     * @param productId 商品ID
     */
    void deleteProduct(String productId);

    /**
     * 查询商品详情（含属性值和SKU）
     *
     * @param productId 商品ID
     * @return 商品详情VO
     */
    ProductInfoDetailVo getProductDetail(String productId);
}
