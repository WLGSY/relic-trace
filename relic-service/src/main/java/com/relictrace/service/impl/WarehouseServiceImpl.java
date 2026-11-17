package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relictrace.common.BizCodeUtils;
import com.relictrace.common.BusinessException;
import com.relictrace.common.PageResult;
import com.relictrace.mapper.RelicInfoMapper;
import com.relictrace.mapper.RelicStoragePositionMapper;
import com.relictrace.mapper.RelicStockCheckMapper;
import com.relictrace.mapper.RelicStockRecordMapper;
import com.relictrace.mapper.RelicWarehouseMapper;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.po.RelicStoragePositionDO;
import com.relictrace.po.RelicStockCheckDO;
import com.relictrace.po.RelicStockRecordDO;
import com.relictrace.po.RelicWarehouseDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 库房与出入库服务实现。
 *
 * @author 李丰
 */
@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private RelicWarehouseMapper warehouseMapper;

    @Autowired
    private RelicStoragePositionMapper positionMapper;

    @Autowired
    private RelicStockRecordMapper stockRecordMapper;

    @Autowired
    private RelicStockCheckMapper stockCheckMapper;

    @Autowired
    private RelicInfoMapper relicInfoMapper;

    /* ------------------------- 库房 ------------------------- */

    @Override
    public List<RelicWarehouseDO> listWarehouses() {
        return warehouseMapper.selectList(
                new LambdaQueryWrapper<RelicWarehouseDO>().orderByAsc(RelicWarehouseDO::getWarehouseCode)
        );
    }

    @Override
    public String createWarehouse(RelicWarehouseDO warehouse) {
        if (warehouse.getIsEnabled() == null) {
            warehouse.setIsEnabled(1);
        }
        warehouseMapper.insert(warehouse);
        return warehouse.getId();
    }

    @Override
    public void updateWarehouse(String id, RelicWarehouseDO warehouse) {
        if (warehouseMapper.selectById(id) == null) {
            throw new BusinessException("库房不存在或已被删除");
        }
        warehouse.setId(id);
        warehouseMapper.updateById(warehouse);
    }

    @Override
    public void deleteWarehouse(String id) {
        if (warehouseMapper.selectById(id) == null) {
            throw new BusinessException("库房不存在或已被删除");
        }
        // 校验库房下是否仍有藏品
        Long count = relicInfoMapper.selectCount(
                new LambdaQueryWrapper<RelicInfoDO>().eq(RelicInfoDO::getFkWarehouseId, id)
        );
        if (count != null && count > 0) {
            throw new BusinessException("该库房下仍有藏品，无法删除");
        }
        warehouseMapper.deleteById(id);
    }

    /* ------------------------- 仓位 ------------------------- */

    @Override
    public List<RelicStoragePositionDO> listPositions(String fkWarehouseId) {
        LambdaQueryWrapper<RelicStoragePositionDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(fkWarehouseId)) {
            wrapper.eq(RelicStoragePositionDO::getFkWarehouseId, fkWarehouseId);
        }
        wrapper.orderByAsc(RelicStoragePositionDO::getPositionCode);
        return positionMapper.selectList(wrapper);
    }

    @Override
    public String createPosition(RelicStoragePositionDO position) {
        if (position.getIsEnabled() == null) {
            position.setIsEnabled(1);
        }
        positionMapper.insert(position);
        return position.getId();
    }

    @Override
    public void updatePosition(String id, RelicStoragePositionDO position) {
        if (positionMapper.selectById(id) == null) {
            throw new BusinessException("仓位不存在或已被删除");
        }
        position.setId(id);
        positionMapper.updateById(position);
    }

    @Override
    public void deletePosition(String id) {
        if (positionMapper.selectById(id) == null) {
            throw new BusinessException("仓位不存在或已被删除");
        }
        positionMapper.deleteById(id);
    }

    /* ------------------------- 出入库记录 ------------------------- */

    @Override
    public PageResult<RelicStockRecordDO> pageStockRecords(PageQuery query) {
        LambdaQueryWrapper<RelicStockRecordDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(RelicStockRecordDO::getRecordCode, query.getKeyword());
        }
        wrapper.orderByDesc(RelicStockRecordDO::getCreateTime);
        IPage<RelicStockRecordDO> page = stockRecordMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public String createStockRecord(RelicStockRecordDO record) {
        if (record.getApplyStatus() == null) {
            record.setApplyStatus(0);
        }
        // 单号由后端统一生成：入库 RK-、出库 CK-、移库 YK-，各自独立流水
        if (!StringUtils.hasText(record.getRecordCode())) {
            String typePrefix = switch (record.getRecordType() == null ? 1 : record.getRecordType()) {
                case 2 -> "CK-";
                case 3 -> "YK-";
                default -> "RK-";
            };
            String prefix = typePrefix + LocalDate.now().getYear() + "-";
            record.setRecordCode(BizCodeUtils.nextCode(stockRecordMapper.selectMaxRecordCode(prefix), prefix));
        }
        stockRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public void updateStockRecord(String id, RelicStockRecordDO record) {
        if (stockRecordMapper.selectById(id) == null) {
            throw new BusinessException("出入库记录不存在或已被删除");
        }
        record.setId(id);
        stockRecordMapper.updateById(record);
    }

    @Override
    public void deleteStockRecord(String id) {
        if (stockRecordMapper.selectById(id) == null) {
            throw new BusinessException("出入库记录不存在或已被删除");
        }
        stockRecordMapper.deleteById(id);
    }

    /* ------------------------- 库存盘点 ------------------------- */

    @Override
    public PageResult<RelicStockCheckDO> pageStockChecks(PageQuery query) {
        LambdaQueryWrapper<RelicStockCheckDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(RelicStockCheckDO::getCheckCode, query.getKeyword());
        }
        wrapper.orderByDesc(RelicStockCheckDO::getCreateTime);
        IPage<RelicStockCheckDO> page = stockCheckMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public String createStockCheck(RelicStockCheckDO check) {
        if (check.getCheckStatus() == null) {
            check.setCheckStatus(1);
        }
        if (check.getAbnormalCount() == null) {
            check.setAbnormalCount(0);
        }
        // 自动统计应盘数量（库房内在库藏品数）
        if (check.getTotalCount() == null && StringUtils.hasText(check.getFkWarehouseId())) {
            Long total = relicInfoMapper.selectCount(
                    new LambdaQueryWrapper<RelicInfoDO>()
                            .eq(RelicInfoDO::getFkWarehouseId, check.getFkWarehouseId())
                            .eq(RelicInfoDO::getStockStatus, 1)
            );
            check.setTotalCount(total == null ? 0 : total.intValue());
        }
        stockCheckMapper.insert(check);
        return check.getId();
    }

    @Override
    public void finishStockCheck(String id, RelicStockCheckDO check) {
        RelicStockCheckDO exist = stockCheckMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("盘点记录不存在或已被删除");
        }
        check.setId(id);
        check.setCheckStatus(2);
        // 异常数量 = 应盘 - 实盘
        int total = check.getTotalCount() == null ? (exist.getTotalCount() == null ? 0 : exist.getTotalCount()) : check.getTotalCount();
        int real = check.getRealCount() == null ? total : check.getRealCount();
        check.setAbnormalCount(Math.max(0, total - real));
        stockCheckMapper.updateById(check);
    }
}
