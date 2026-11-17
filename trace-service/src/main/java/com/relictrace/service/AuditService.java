package com.relictrace.service;

import com.relictrace.common.PageResult;
import com.relictrace.dto.AuditDTO;
import com.relictrace.po.SysAuditRecordDO;
import com.relictrace.query.AuditQuery;
import com.relictrace.query.PageQuery;
import com.relictrace.vo.AuditItemVO;
import com.relictrace.vo.PendingCountVO;

import java.util.List;
import java.util.Map;

/**
 * 审核服务接口。
 *
 * @author 李丰
 */
public interface AuditService {

    /**
     * 查询待审核列表（跨业务类型汇总，可按业务类型过滤）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<AuditItemVO> listPending(AuditQuery query);

    /**
     * 查询各业务类型的待审核数量。
     *
     * @return 待审核数量汇总
     */
    PendingCountVO getPendingCount();

    /**
     * 分页查询审核记录。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SysAuditRecordDO> listAuditRecords(PageQuery query);

    /**
     * 提交审核。
     *
     * @param auditDTO 审核参数
     */
    void submitAudit(AuditDTO auditDTO);

    /**
     * 批量审核。
     *
     * @param items        待审核项列表
     * @param auditOpinion 审核意见
     */
    void batchAudit(List<Map<String, Object>> items, String auditOpinion);
}
