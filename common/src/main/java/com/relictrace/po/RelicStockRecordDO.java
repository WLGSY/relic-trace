package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 出入库记录实体（relic_stock_record）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_stock_record")
public class RelicStockRecordDO extends VersionedEntity {

    /** 出入库记录 id（主键） */
    @TableId
    private String id;

    /** 单据编号（唯一） */
    private String recordCode;

    /** 业务类型：1 入库 / 2 出库 / 3 移库 */
    private Integer recordType;

    /** 藏品 id（关联 relic_info） */
    private String fkRelicId;

    /** 来源或去向（出库填去向，入库填来源） */
    private String sourceTarget;

    /** 原库房 id（关联 relic_warehouse） */
    private String fkFromWarehouseId;

    /** 目标库房 id（关联 relic_warehouse） */
    private String fkToWarehouseId;

    /** 经办人 */
    private String operatorName;

    /** 经办时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime operateTime;

    /** 事由 */
    private String operateReason;

    /** 申请状态：0 待审核 / 1 审核通过 / 2 审核驳回 */
    private Integer applyStatus;

    /** 审核意见 */
    private String auditOpinion;
}
