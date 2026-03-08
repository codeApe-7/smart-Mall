package com.smartMall.controller;

import com.smartMall.entities.dto.ProductKnowledgeQueryDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductKnowledgeVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.ProductKnowledgeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product knowledge controller for RAG retrieval.
 */
@Slf4j
@RestController
@RequestMapping("/product/knowledge")
public class MallProductKnowledgeController {

    @Resource
    private ProductKnowledgeService productKnowledgeService;

    @PostMapping("/search")
    public ResponseVO<PageResultVO<ProductKnowledgeVO>> search(@RequestBody(required = false) ProductKnowledgeQueryDTO dto) {
        ProductKnowledgeQueryDTO safeQuery = dto == null ? new ProductKnowledgeQueryDTO() : dto;
        log.info("web search product knowledge, keyword={}, productId={}", safeQuery.getKeyword(), safeQuery.getProductId());
        return ResponseVO.success(productKnowledgeService.searchKnowledge(safeQuery));
    }

    @GetMapping("/detail/{productId}")
    public ResponseVO<ProductKnowledgeVO> detail(@PathVariable String productId) {
        log.info("web load product knowledge detail, productId={}", productId);
        return ResponseVO.success(productKnowledgeService.getKnowledgeDetail(productId));
    }
}
