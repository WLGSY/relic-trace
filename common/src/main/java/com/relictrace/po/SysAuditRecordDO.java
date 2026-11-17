package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录实体（sys_audit_record）。
 *
 * @author 李丰
 */
@Data
@TableName("sys_audit_record")
public class SysAuditRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 审核记录 id（主键） */
    @TableId
    private String id;

    /** 业务类型：1 征集审核 / 2 出入库审核 / 3 修复审核 / 4 借调审核 / 5 注销审核 */
    private Integer bizType;

    /** 业务单据 id（关联对应业务表 id） */
    private String fkBizId;

    /** 业务单号 */
    private String bizCode;

    /** 审核人 id */
    private String auditUserId;

    /** 审核人 */
    private String auditUserName;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime auditTime;

    /** 审核结果：1 通过 / 2 驳回 */
    private Integer auditResult;

    /** 审核意见（驳回时必填） */
    private String auditOpinion;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /** 是否删除：0 未删除 / 1 已删除 */
    private Integer isDeleted;

    /** 业务类型名称（非表字段，查询时按 bizType 翻译，供审核记录列表展示） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String bizTypeName;
}
