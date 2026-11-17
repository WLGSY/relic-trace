package com.relictrace.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核提交参数。
 *
 * @author 李丰
 */
@Data
public class AuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务类型：1 征集 / 2 出入库 / 3 修复 / 4 借调 / 5 注销 */
    private Integer bizType;

    /** 业务单据 id */
    private String fkBizId;

    /** 业务单据 id（前端待办列表字段名 bizId，作为 fkBizId 的别名兼容） */
    private String bizId;

    /** 业务单号 */
    private String bizCode;

    /** 审核结果：1 通过 / 2 驳回 */
    private Integer auditResult;

    /** 审核意见（驳回时必填） */
    private String auditOpinion;

    /**
     * 统一取业务单据 id。
     *
     * <p>待办列表返回的字段名是 {@code bizId}，历史接口用的是 {@code fkBizId}，
     * 这里做一次归一，避免调用方字段名不一致导致「审核对象不完整」。</p>
     *
     * @return 业务单据 id
     */
    public String getFkBizId() {
        return fkBizId != null && !fkBizId.isEmpty() ? fkBizId : bizId;
    }
}
