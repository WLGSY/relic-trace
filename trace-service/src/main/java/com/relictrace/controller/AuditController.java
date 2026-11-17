package com.relictrace.controller;

import com.relictrace.common.PageResult;
import com.relictrace.common.RequireRole;
import com.relictrace.common.Result;
import com.relictrace.dto.AuditDTO;
import com.relictrace.po.SysAuditRecordDO;
import com.relictrace.query.AuditQuery;
import com.relictrace.query.PageQuery;
import com.relictrace.service.AuditService;
import com.relictrace.vo.AuditItemVO;
import com.relictrace.vo.PendingCountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 审核工作台控制层。
 *
 * @author 李丰
 */
@RestController
@RequestMapping("/audits")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /** 待审核列表（跨业务类型，可按 bizType 过滤） */
    @GetMapping("/pending")
    public Result<PageResult<AuditItemVO>> listPending(AuditQuery query) {
        return Result.success(auditService.listPending(query));
    }

    /** 待审核数量 */
    @GetMapping("/pending-count")
    public Result<PendingCountVO> pendingCount() {
        return Result.success(auditService.getPendingCount());
    }

    /** 审核记录列表 */
    @GetMapping
    public Result<PageResult<SysAuditRecordDO>> listAuditRecords(PageQuery query) {
        return Result.success(auditService.listAuditRecords(query));
    }

    /** 提交审核（仅系统管理员、审核员） */
    @RequireRole({1, 3})
    @PostMapping
    public Result<Void> submitAudit(@RequestBody AuditDTO auditDTO) {
        auditService.submitAudit(auditDTO);
        return Result.success(null, "审核完成");
    }

    /** 批量审核（仅系统管理员、审核员） */
    @RequireRole({1, 3})
    @PostMapping("/batch")
    public Result<Void> batchAudit(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        String auditOpinion = (String) body.get("auditOpinion");
        auditService.batchAudit(items, auditOpinion);
        return Result.success(null, "批量审核完成");
    }
}
