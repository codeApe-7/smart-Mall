package com.smartMall.entities.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Admin review reply DTO.
 */
@Data
public class AdminReviewReplyDTO {

    @NotBlank(message = "reviewId can not be blank")
    private String reviewId;

    @NotBlank(message = "replyContent can not be blank")
    private String replyContent;
}
