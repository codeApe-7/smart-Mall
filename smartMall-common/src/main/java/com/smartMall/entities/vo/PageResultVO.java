package com.smartMall.entities.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页结果包装类
 *
 * @author <a href="https://github.com/aiaicoder"> 小新
 * @version 1.0
 * @date 2026/1/24 23:40
 */
@Data
public class PageResultVO<T> {

    /**
     * 当前页码
     */
    private Integer pageNo;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long totalCount;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 数据列表
     */
    private List<T> records;

    public PageResultVO() {
    }

    public PageResultVO(Integer pageNo, Integer pageSize, Long totalCount, List<T> records) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.records = records;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
    }

    /**
     * 从 MyBatis-Plus 的 IPage 对象转换
     */
    public static <T> PageResultVO<T> fromPage(IPage<T> page) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setPageNo((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setTotalCount(page.getTotal());
        result.setTotalPages((int) page.getPages());
        result.setRecords(page.getRecords());
        return result;
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResultVO<T> empty(Integer pageNo, Integer pageSize) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        result.setTotalCount(0L);
        result.setTotalPages(0);
        result.setRecords(List.of());
        return result;
    }
}
