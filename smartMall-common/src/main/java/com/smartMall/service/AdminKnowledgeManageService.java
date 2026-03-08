package com.smartMall.service;

import com.smartMall.entities.vo.AdminKnowledgeIndexSummaryVO;
import com.smartMall.entities.vo.AdminKnowledgeIndexSyncResultVO;
import com.smartMall.entities.vo.ProductKnowledgeVO;

/**
 * Admin knowledge manage service.
 */
public interface AdminKnowledgeManageService {

    /**
     * Load product knowledge card for admin preview.
     *
     * @param productId product id
     * @return knowledge card
     */
    ProductKnowledgeVO getProductKnowledge(String productId);

    /**
     * Load search index summary.
     *
     * @return summary
     */
    AdminKnowledgeIndexSummaryVO getIndexSummary();

    /**
     * Sync one product to search index. Off-sale products will be deleted from index.
     *
     * @param productId product id
     * @return sync result
     */
    AdminKnowledgeIndexSyncResultVO syncProduct(String productId);

    /**
     * Rebuild all on-sale products into search index.
     *
     * @return rebuild result
     */
    AdminKnowledgeIndexSyncResultVO rebuildIndex();
}
