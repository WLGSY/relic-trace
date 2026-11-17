package com.relictrace.service;

import com.relictrace.common.PageResult;
import com.relictrace.po.RelicCancelApplyDO;
import com.relictrace.query.PageQuery;

/**
 * 注销申请服务接口。
 *
 * @author 李丰
 */
public interface CancelService {

    /** 分页查询注销申请 */
    PageResult<RelicCancelApplyDO> page(PageQuery query);

    /** 新增注销申请 */
    String create(RelicCancelApplyDO cancel);

    /** 归档（审核通过后系统管理员执行） */
    void archive(String id);
}
