package com.smartMall.service;

import com.smartMall.entities.dto.AdminReviewQueryDTO;
import com.smartMall.entities.dto.AdminReviewReplyDTO;
import com.smartMall.entities.vo.AdminReviewInfoVO;
import com.smartMall.entities.vo.PageResultVO;

/**
 * Admin review manage service.
 */
public interface AdminReviewManageService {

    PageResultVO<AdminReviewInfoVO> loadReviewList(AdminReviewQueryDTO dto);

    AdminReviewInfoVO getReviewDetail(String reviewId);

    void replyReview(AdminReviewReplyDTO dto);

    void deleteReview(String reviewId);
}
