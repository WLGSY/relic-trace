package com.relictrace.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果封装。
 *
 * <p>字段名与前端分页组件约定保持一致：{@code list} 为当前页数据，{@code total} 为总记录数。</p>
 *
 * @param <T> 数据行类型
 * @author 李丰
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据列表 */
    private List<T> list;

    /** 总记录数 */
    private Long total;

    /** 页码 */
    private Long pageNum;

    /** 每页条数 */
    private Long pageSize;

    public PageResult() {
        this.list = Collections.emptyList();
        this.total = 0L;
    }

    public PageResult(List<T> list, Long total, Long pageNum, Long pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
