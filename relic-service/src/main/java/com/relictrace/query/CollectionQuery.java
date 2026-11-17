package com.relictrace.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 藏品征集查询条件对象。
 *
 * <p>在通用分页参数基础上，补充征集列表的高级查询条件：
 * 状态、分类、年代、征集方式与征集时间区间。</p>
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CollectionQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    /** 申请状态：0 草稿 / 1 待审核 / 2 通过 / 3 驳回 */
    private Integer applyStatus;

    /** 藏品分类 id */
    private String categoryId;

    /** 年代 */
    private String era;

    /** 征集方式（字典码：donate / purchase / transfer / borrow） */
    private String collectionWay;

    /** 征集时间区间起点（yyyy-MM-dd） */
    private String beginTime;

    /** 征集时间区间终点（yyyy-MM-dd） */
    private String endTime;
}
