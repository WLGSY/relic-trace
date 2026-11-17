package com.relictrace.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页查询条件基类。
 *
 * @author 李丰
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码（从 1 开始） */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 关键字 */
    private String keyword;
}
