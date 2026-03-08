package com.smartMall.service;

import com.smartMall.entities.dto.ProductKnowledgeQueryDTO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ProductKnowledgeVO;

/**
 * Product knowledge service for RAG retrieval.
 */
public interface ProductKnowledgeService {

    PageResultVO<ProductKnowledgeVO> searchKnowledge(ProductKnowledgeQueryDTO dto);

    ProductKnowledgeVO getKnowledgeDetail(String productId);
}
