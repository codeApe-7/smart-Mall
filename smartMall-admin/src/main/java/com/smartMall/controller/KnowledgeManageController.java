package com.smartMall.controller;

import com.smartMall.entities.vo.AdminKnowledgeIndexSummaryVO;
import com.smartMall.entities.vo.AdminKnowledgeIndexSyncResultVO;
import com.smartMall.entities.vo.ProductKnowledgeVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.AdminKnowledgeManageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin knowledge manage controller.
 */
@RestController
@RequestMapping("/knowledge")
public class KnowledgeManageController {

    @Resource
    private AdminKnowledgeManageService adminKnowledgeManageService;

    @GetMapping("/product/{productId}")
    public ResponseVO<ProductKnowledgeVO> product(@PathVariable String productId) {
        return ResponseVO.success(adminKnowledgeManageService.getProductKnowledge(productId));
    }

    @GetMapping("/index/summary")
    public ResponseVO<AdminKnowledgeIndexSummaryVO> summary() {
        return ResponseVO.success(adminKnowledgeManageService.getIndexSummary());
    }

    @PostMapping("/index/sync/{productId}")
    public ResponseVO<AdminKnowledgeIndexSyncResultVO> sync(@PathVariable String productId) {
        return ResponseVO.success(adminKnowledgeManageService.syncProduct(productId));
    }

    @PostMapping("/index/rebuild")
    public ResponseVO<AdminKnowledgeIndexSyncResultVO> rebuild() {
        return ResponseVO.success(adminKnowledgeManageService.rebuildIndex());
    }
}
