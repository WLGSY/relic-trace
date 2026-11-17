package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 注销申请实体（relic_cancel_apply）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_cancel_apply")
public class RelicCancelApplyDO extends BaseEntity {

    /** 注销申请 id（主键） */
    @TableId
    private String id;

    /** 注销单号（唯一） */
    private String applyCode;

    /** 藏品 id（关联 relic_info） */
    private String fkRelicId;

    /** 注销原因 */
    private String cancelReason;

    /** 经办人 */
    private String cancelUserName;

    /** 申请状态：0 待审核 / 1 审核通过 / 2 审核驳回 */
    private Integer applyStatus;

    /** 审核意见 */
    private String auditOpinion;

    /** 归档人（审核通过后系统管理员执行归档） */
    private String archiveUserName;

    /** 归档时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime archiveTime;

    /** 乐观锁版本号 */
    private Integer version;
}
