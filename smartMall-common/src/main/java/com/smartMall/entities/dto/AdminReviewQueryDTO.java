package com.smartMall.entities.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Admin review query DTO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminReviewQueryDTO extends PageQueryDTO {

    private String orderNo;

    private String productId;

    private String userId;

    private Integer rating;

    private Boolean replied;
}
