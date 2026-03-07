package com.smartMall.controller;

import com.smartMall.entities.dto.ProductQueryDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductInfoDetailVo;
import com.smartMall.entities.vo.ProductInfoListVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.ProductInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端商品浏览控制器。
 */
@Slf4j
@RestController
@RequestMapping("/product")
public class MallProductController {

    @Resource
    private ProductInfoService productInfoService;

    /**
     * 分页查询已上架商品列表。
     *
     * @param queryDTO 查询参数
     * @return 商品分页结果
     */
    @PostMapping("/list")
    public ResponseVO<PageResultVO<ProductInfoListVO>> list(@RequestBody(required = false) ProductQueryDTO queryDTO) {
        ProductQueryDTO safeQuery = queryDTO == null ? new ProductQueryDTO() : queryDTO;
        log.info("web load product list, pageNo={}, pageSize={}", safeQuery.getPageNo(), safeQuery.getPageSize());
        return ResponseVO.success(productInfoService.loadVisibleProductList(safeQuery));
    }

    /**
     * 查询推荐商品。
     *
     * @param limit 返回数量
     * @return 推荐商品列表
     */
    @GetMapping("/recommend")
    public ResponseVO<List<ProductInfoListVO>> recommend(@RequestParam(required = false) Integer limit) {
        log.info("web load recommend products, limit={}", limit);
        return ResponseVO.success(productInfoService.loadRecommendProducts(limit));
    }

    /**
     * 查询已上架商品详情。
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/detail/{productId}")
    public ResponseVO<ProductInfoDetailVo> detail(@PathVariable String productId) {
        log.info("web load product detail, productId={}", productId);
        return ResponseVO.success(productInfoService.getVisibleProductDetail(productId));
    }
}
