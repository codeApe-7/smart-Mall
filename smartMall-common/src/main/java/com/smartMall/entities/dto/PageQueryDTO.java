package com.smartMall.entities.dto;

import lombok.Data;

/**
 * 分页查询参数基类
 *
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/24 23:40
 */
@Data
public class PageQueryDTO {

    /**
     * 当前页码，默认第1页
     */
    private Integer pageNo = 1;

    /**
     * 每页条数，默认10条
     */
    private Integer pageSize = 10;

    /**
     * 获取偏移量（用于分页查询）
     */
    public Integer getOffset() {
        return (pageNo - 1) * pageSize;
    }
}
