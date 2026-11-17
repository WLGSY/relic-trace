package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 修复申请实体（relic_repair_apply）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_repair_apply")
public class RelicRepairApplyDO extends VersionedEntity {

    /** 修复申请 id（主键） */
    @TableId
    private String id;

    /** 修复单号（唯一） */
    private String applyCode;

    /** 藏品 id（关联 relic_info） */
    private String fkRelicId;

    /** 修复项目 */
    private String repairItem;

    /** 修复人 */
    private String repairUserName;

    /** 修复开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;

    /** 修复结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;

    /** 修复前状况 */
    private String conditionBefore;

    /** 修复后状况（审核通过后填写） */
    private String conditionAfter;

    /** 申请状态：0 草稿 / 1 待审核 / 2 审核通过 / 3 审核驳回 */
    private Integer applyStatus;

    /** 审核意见 */
    private String auditOpinion;
}
