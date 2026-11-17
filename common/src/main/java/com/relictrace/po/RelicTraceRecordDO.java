package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 溯源记录实体（relic_trace_record）。
 *
 * @author 李丰
 */
@Data
@TableName("relic_trace_record")
public class RelicTraceRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 溯源记录 id（主键） */
    @TableId
    private String id;

    /** 藏品 id（关联 relic_info） */
    private String fkRelicId;

    /** 溯源类型：1 征集 / 2 入藏 / 3 入库 / 4 出库 / 5 移库 / 6 修复 / 7 借出 / 8 归还 / 9 注销 */
    private Integer traceType;

    /** 关联业务 id（对应业务单据 id） */
    private String fkBizId;

    /** 溯源时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime traceTime;

    /** 操作人 */
    private String operatorName;

    /** 溯源内容 */
    private String traceContent;

    /** 创建人 id */
    private String createUserId;

    /** 创建人 */
    private String createUserName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /** 是否删除：0 未删除 / 1 已删除 */
    private Integer isDeleted;
}
