package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 库存盘点实体（relic_stock_check）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_stock_check")
public class RelicStockCheckDO extends BaseEntity {

    /** 盘点 id（主键） */
    @TableId
    private String id;

    /** 盘点单号（唯一） */
    private String checkCode;

    /** 盘点库房 id（关联 relic_warehouse） */
    private String fkWarehouseId;

    /** 盘点人 */
    private String checkUserName;

    /** 盘点时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime checkTime;

    /** 应盘数量 */
    private Integer totalCount;

    /** 实盘数量 */
    private Integer realCount;

    /** 异常数量（默认为 0） */
    private Integer abnormalCount;

    /** 盘点状态：1 盘点中 / 2 已完成 */
    private Integer checkStatus;

    /** 盘点结果说明 */
    private String checkResult;

    /** 乐观锁版本号 */
    private Integer version;
}
