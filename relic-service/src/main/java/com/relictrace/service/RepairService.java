package com.relictrace.service;

import com.relictrace.common.PageResult;
import com.relictrace.po.RelicRepairApplyDO;
import com.relictrace.query.PageQuery;

/**
 * 修复申请服务接口。
 *
 * @author 李丰
 */
public interface RepairService {

    /** 分页查询修复申请 */
    PageResult<RelicRepairApplyDO> page(PageQuery query);

    /** 新增修复申请 */
    String create(RelicRepairApplyDO repair);

    /** 处理修复（填写修复结果，置为待审核或完成） */
    void process(String id, RelicRepairApplyDO repair);

    /** 删除修复申请 */
    void delete(String id);
}
