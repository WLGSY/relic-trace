package com.relictrace.service;

import com.relictrace.common.PageResult;
import com.relictrace.po.RelicStoragePositionDO;
import com.relictrace.po.RelicStockCheckDO;
import com.relictrace.po.RelicStockRecordDO;
import com.relictrace.po.RelicWarehouseDO;
import com.relictrace.query.PageQuery;

import java.util.List;

/**
 * 库房与出入库服务接口。
 *
 * @author 李丰
 */
public interface WarehouseService {

    /* ------------------------- 库房 ------------------------- */

    /** 查询全部库房 */
    List<RelicWarehouseDO> listWarehouses();

    /** 新增库房 */
    String createWarehouse(RelicWarehouseDO warehouse);

    /** 修改库房 */
    void updateWarehouse(String id, RelicWarehouseDO warehouse);

    /** 删除库房 */
    void deleteWarehouse(String id);

    /* ------------------------- 仓位 ------------------------- */

    /** 查询仓位（可按库房过滤） */
    List<RelicStoragePositionDO> listPositions(String fkWarehouseId);

    /** 新增仓位 */
    String createPosition(RelicStoragePositionDO position);

    /** 修改仓位 */
    void updatePosition(String id, RelicStoragePositionDO position);

    /** 删除仓位 */
    void deletePosition(String id);

    /* ------------------------- 出入库记录 ------------------------- */

    /** 分页查询出入库记录 */
    PageResult<RelicStockRecordDO> pageStockRecords(PageQuery query);

    /** 新增出入库记录 */
    String createStockRecord(RelicStockRecordDO record);

    /** 修改出入库记录 */
    void updateStockRecord(String id, RelicStockRecordDO record);

    /** 删除出入库记录 */
    void deleteStockRecord(String id);

    /* ------------------------- 库存盘点 ------------------------- */

    /** 分页查询库存盘点 */
    PageResult<RelicStockCheckDO> pageStockChecks(PageQuery query);

    /** 新增库存盘点 */
    String createStockCheck(RelicStockCheckDO check);

    /** 完成盘点 */
    void finishStockCheck(String id, RelicStockCheckDO check);
}
