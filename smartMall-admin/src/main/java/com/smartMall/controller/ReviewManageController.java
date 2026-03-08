package com.smartMall.controller;

import com.smartMall.entities.dto.AdminReviewQueryDTO;
import com.smartMall.entities.dto.AdminReviewReplyDTO;
import com.smartMall.entities.vo.AdminReviewInfoVO;
import com.smartMall.entities.vo.PageResultVO;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.service.AdminReviewManageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin review manage controller.
 */
@RestController
@RequestMapping("/review")
public class ReviewManageController {

    @Resource
    private AdminReviewManageService adminReviewManageService;

    @PostMapping("/list")
    public ResponseVO<PageResultVO<AdminReviewInfoVO>> list(@RequestBody(required = false) AdminReviewQueryDTO dto) {
        return ResponseVO.success(adminReviewManageService.loadReviewList(dto));
    }

    @GetMapping("/detail/{reviewId}")
    public ResponseVO<AdminReviewInfoVO> detail(@PathVariable String reviewId) {
        return ResponseVO.success(adminReviewManageService.getReviewDetail(reviewId));
    }

    @PostMapping("/reply")
    public ResponseVO<Void> reply(@RequestBody @Valid AdminReviewReplyDTO dto) {
        adminReviewManageService.replyReview(dto);
        return ResponseVO.success();
    }

    @PostMapping("/delete/{reviewId}")
    public ResponseVO<Void> delete(@PathVariable String reviewId) {
        adminReviewManageService.deleteReview(reviewId);
        return ResponseVO.success();
    }
}
