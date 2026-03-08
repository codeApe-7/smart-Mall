package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 后台管理员账号角色关联表。
 */
@Data
@TableName("sys_admin_account_role")
public class SysAdminAccountRole implements Serializable {

    @TableId("rel_id")
    private String relId;

    @TableField("account_id")
    private String accountId;

    @TableField("role_id")
    private String roleId;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
