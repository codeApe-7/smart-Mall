package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 后台管理员账号表。
 */
@Data
@TableName("sys_admin_account")
public class SysAdminAccount implements Serializable {

    @TableId("account_id")
    private String accountId;

    @TableField("account_name")
    private String accountName;

    @TableField("password")
    private String password;

    @TableField("nickname")
    private String nickname;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("status")
    private Integer status;

    @TableField("super_admin")
    private Integer superAdmin;

    @TableField("remark")
    private String remark;

    @TableField("last_login_time")
    private Date lastLoginTime;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
