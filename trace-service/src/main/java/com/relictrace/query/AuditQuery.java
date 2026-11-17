package com.relictrace.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核待办查询条件。
 *
 * <p>在通用分页条件上追加业务类型，用于审核工作台顶部五个分类卡片
 * 切换时只查看某一类待审业务（1 征集 / 2 出入库 / 3 修复 / 4 借调 / 5 注销）。</p>
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    /** 业务类型：1 征集 / 2 出入库 / 3 修复 / 4 借调 / 5 注销，为空表示不限 */
    private Integer bizType;
}
