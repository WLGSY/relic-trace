package com.relictrace.controller;

import com.relictrace.common.PageResult;
import com.relictrace.common.Result;
import com.relictrace.po.RelicStoragePositionDO;
import com.relictrace.po.RelicStockCheckDO;
import com.relictrace.po.RelicStockRecordDO;
import com.relictrace.po.RelicWarehouseDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库房与出入库控制层。
 *
 * @author 李丰
 */
@RestController
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    /* ------------------------- 库房 ------------------------- */

    @GetMapping("/warehouses")
    public Result<List<RelicWarehouseDO>> listWarehouses() {
        return Result.success(warehouseService.listWarehouses());
    }

    @PostMapping("/warehouses")
    public Result<String> createWarehouse(@RequestBody RelicWarehouseDO warehouse) {
        return Result.success(warehouseService.createWarehouse(warehouse), "新增成功");
    }

    @PutMapping("/warehouses/{id}")
    public Result<Void> updateWarehouse(@PathVariable String id, @RequestBody RelicWarehouseDO warehouse) {
        warehouseService.updateWarehouse(id, warehouse);
        return Result.success(null, "修改成功");
    }

    @DeleteMapping("/warehouses/{id}")
    public Result<Void> deleteWarehouse(@PathVariable String id) {
        warehouseService.deleteWarehouse(id);
        return Result.success(null, "删除成功");
    }

    /* ------------------------- 仓位 ------------------------- */

    @GetMapping("/positions")
    public Result<List<RelicStoragePositionDO>> listPositions(@RequestParam(required = false) String fkWarehouseId) {
        return Result.success(warehouseService.listPositions(fkWarehouseId));
    }

    @PostMapping("/positions")
    public Result<String> createPosition(@RequestBody RelicStoragePositionDO position) {
        return Result.success(warehouseService.createPosition(position), "新增成功");
    }

    @PutMapping("/positions/{id}")
    public Result<Void> updatePosition(@PathVariable String id, @RequestBody RelicStoragePositionDO position) {
        warehouseService.updatePosition(id, position);
        return Result.success(null, "修改成功");
    }

    @DeleteMapping("/positions/{id}")
    public Result<Void> deletePosition(@PathVariable String id) {
        warehouseService.deletePosition(id);
        return Result.success(null, "删除成功");
    }

    /* ------------------------- 出入库记录 ------------------------- */

    @GetMapping("/stock-records")
    public Result<PageResult<RelicStockRecordDO>> listStockRecords(PageQuery query) {
        return Result.success(warehouseService.pageStockRecords(query));
    }

    @PostMapping("/stock-records")
    public Result<String> createStockRecord(@RequestBody RelicStockRecordDO record) {
        return Result.success(warehouseService.createStockRecord(record), "新增成功");
    }

    @PutMapping("/stock-records/{id}")
    public Result<Void> updateStockRecord(@PathVariable String id, @RequestBody RelicStockRecordDO record) {
        warehouseService.updateStockRecord(id, record);
        return Result.success(null, "修改成功");
    }

    @DeleteMapping("/stock-records/{id}")
    public Result<Void> deleteStockRecord(@PathVariable String id) {
        warehouseService.deleteStockRecord(id);
        return Result.success(null, "删除成功");
    }

    /* ------------------------- 库存盘点 ------------------------- */

    @GetMapping("/stock-checks")
    public Result<PageResult<RelicStockCheckDO>> listStockChecks(PageQuery query) {
        return Result.success(warehouseService.pageStockChecks(query));
    }

    @PostMapping("/stock-checks")
    public Result<String> createStockCheck(@RequestBody RelicStockCheckDO check) {
        return Result.success(warehouseService.createStockCheck(check), "新增成功");
    }

    @PutMapping("/stock-checks/{id}")
    public Result<Void> finishStockCheck(@PathVariable String id, @RequestBody RelicStockCheckDO check) {
        warehouseService.finishStockCheck(id, check);
        return Result.success(null, "盘点完成");
    }
}
