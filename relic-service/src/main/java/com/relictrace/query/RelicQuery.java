package com.relictrace.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 藏品查询条件对象。
 *
 * <p>封装后台分页查询与公开检索的筛选参数。</p>
 *
 * @author 李丰
 */
@Data
public class RelicQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 页码（从 1 开始） */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 关键字（藏品名称 / 编号模糊匹配） */
    private String keyword;

    /** 藏品分类 id */
    private String fkCategoryId;

    /** 年代 */
    private String era;

    /** 藏品级别 */
    private Integer relicLevel;

    /** 库存状态 */
    private Integer stockStatus;

    /** 公开范围：1 公开展示 / 0 保密（后台筛选使用） */
    private Integer isPublic;

    /** 排序方式：level / oldest / views */
    private String sort;
}
