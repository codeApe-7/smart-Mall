package com.smartMall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartMall.entities.domain.ProductInfo;
import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.dto.ProductSaveDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;

import java.util.List;

/**
 * @author 15712
 * @description 针对表【product_info(商品信息)】的数据库操作Service
 * @createDate 2026-02-13 15:52:46
 */
public interface ProductInfoService extends IService<ProductInfo> {

    /**
     * 分页查询商品列表，含分类名称、SKU 数量、总库存
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResultVO<ProductInfoListVO> loadProductList(ProductQueryDTO queryDTO);

    /**
     * 查询用户端可见商品列表，只返回已上架商品
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResultVO<ProductInfoListVO> loadVisibleProductList(ProductQueryDTO queryDTO);

    /**
     * 保存商品，支持新增或更新
     *
     * @param productSaveDTO 商品保存 DTO
     */
    void saveProduct(ProductSaveDTO productSaveDTO);

    /**
     * 删除商品，级联删除属性值和 SKU
     *
     * @param productId 商品ID
     */
    void deleteProduct(String productId);

    /**
     * 查询商品详情，含属性值和 SKU
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    ProductInfoDetailVo getProductDetail(String productId);

    /**
     * 查询用户端可见商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    ProductInfoDetailVo getVisibleProductDetail(String productId);

    /**
     * 查询推荐商品列表
     *
     * @param limit 返回条数
     * @return 推荐商品
     */
    List<ProductInfoListVO> loadRecommendProducts(Integer limit);
}
