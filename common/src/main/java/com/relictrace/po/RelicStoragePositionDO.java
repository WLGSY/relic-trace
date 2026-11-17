package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓位信息实体（relic_storage_position）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_storage_position")
public class RelicStoragePositionDO extends VersionedEntity {

    /** 仓位 id（主键） */
    @TableId
    private String id;

    /** 仓位编号（库房内唯一） */
    private String positionCode;

    /** 仓位名称 */
    private String positionName;

    /** 所属库房 id（关联 relic_warehouse） */
    private String fkWarehouseId;

    /** 是否启用：1 是 / 0 否 */
    private Integer isEnabled;
}
