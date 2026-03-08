package com.smartMall.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Admin user list VO.
 */
@Data
public class AdminUserListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;

    private String username;

    private String nickname;

    private String phone;

    private Integer status;

    private String statusDesc;

    private Long orderCount;

    private BigDecimal totalOrderAmount;

    private Long refundCount;

    private BigDecimal totalRefundAmount;

    private Integer cartItemCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastActiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
