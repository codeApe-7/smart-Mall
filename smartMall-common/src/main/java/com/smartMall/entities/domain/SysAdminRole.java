package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 后台管理员角色表。
 */
@Data
@TableName("sys_admin_role")
public class SysAdminRole implements Serializable {

    @TableId("role_id")
    private String roleId;

    @TableField("role_code")
    private String roleCode;

    @TableField("role_name")
    private String roleName;

    @TableField("permission_codes")
    private String permissionCodes;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
