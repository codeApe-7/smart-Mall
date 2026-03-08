package com.smartMall.entities.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI config persistence entity.
 */
@Data
@TableName("sys_ai_config")
public class SysAiConfig implements Serializable {

    @TableId("config_id")
    private String configId;

    @TableField("config_code")
    private String configCode;

    @TableField("config_name")
    private String configName;

    @TableField("config_content")
    private String configContent;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
