package com.relictrace.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库房信息实体（relic_warehouse）。
 *
 * @author 李丰
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("relic_warehouse")
public class RelicWarehouseDO extends VersionedEntity {

    /** 库房 id（主键） */
    @TableId
    private String id;

    /** 库房编号（唯一） */
    private String warehouseCode;

    /** 库房名称 */
    private String warehouseName;

    /** 库房位置 */
    private String location;

    /** 保管员 */
    private String keeperName;

    /** 是否启用：1 是 / 0 否 */
    private Integer isEnabled;
}
