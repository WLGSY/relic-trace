package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 展览借调实体（relic_exhibition_loan）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_exhibition_loan")
public class RelicExhibitionLoanDO extends VersionedEntity {

    /** 借调 id（主键） */
    @TableId
    private String id;

    /** 借调单号（唯一） */
    private String loanCode;

    /** 藏品 id（关联 relic_info） */
    private String fkRelicId;

    /** 借调单位 */
    private String loanUnit;

    /** 经办人 */
    private String loanUserName;

    /** 借出时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime loanTime;

    /** 预计归还时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime planReturnTime;

    /** 实际归还时间（归还接收后填写） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime actualReturnTime;

    /** 借调状态：1 待审核 / 2 借出中 / 3 已归还 / 4 审核驳回 */
    private Integer loanStatus;

    /** 审核意见 */
    private String auditOpinion;

    /** 借调协议路径 */
    private String agreementPath;
}
