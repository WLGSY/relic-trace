package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relictrace.common.BizCodeUtils;
import com.relictrace.common.BusinessException;
import com.relictrace.common.PageResult;
import com.relictrace.mapper.RelicInfoMapper;
import com.relictrace.mapper.RelicRepairApplyMapper;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.po.RelicRepairApplyDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 修复申请服务实现。
 *
 * @author 李丰
 */
@Service
public class RepairServiceImpl implements RepairService {

    @Autowired
    private RelicRepairApplyMapper repairMapper;

    @Autowired
    private RelicInfoMapper relicInfoMapper;

    @Override
    public PageResult<RelicRepairApplyDO> page(PageQuery query) {
        LambdaQueryWrapper<RelicRepairApplyDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(RelicRepairApplyDO::getApplyCode, query.getKeyword())
                    .or().like(RelicRepairApplyDO::getRepairItem, query.getKeyword()));
        }
        wrapper.orderByDesc(RelicRepairApplyDO::getCreateTime);
        IPage<RelicRepairApplyDO> page = repairMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public String create(RelicRepairApplyDO repair) {
        // 修复申请无独立「提交」入口，创建即进入待审核，否则审核工作台永远收不到修复待办
        if (repair.getApplyStatus() == null) {
            repair.setApplyStatus(1);
        }
        // 单号由后端统一生成，避免调用方漏传导致 apply_code 唯一约束报错
        if (!StringUtils.hasText(repair.getApplyCode())) {
            String prefix = "XF-" + LocalDate.now().getYear() + "-";
            repair.setApplyCode(BizCodeUtils.nextCode(repairMapper.selectMaxApplyCode(prefix), prefix));
        }
        repairMapper.insert(repair);
        return repair.getId();
    }

    @Override
    public void process(String id, RelicRepairApplyDO repair) {
        RelicRepairApplyDO exist = repairMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("修复申请不存在或已被删除");
        }
        repair.setId(id);
        repairMapper.updateById(repair);

        // 填写修复后状况即视为修复完成，藏品回到「在库」
        if (StringUtils.hasText(repair.getConditionAfter()) && exist.getFkRelicId() != null) {
            RelicInfoDO relic = new RelicInfoDO();
            relic.setId(exist.getFkRelicId());
            relic.setStockStatus(1);
            relicInfoMapper.updateById(relic);
        }
    }

    @Override
    public void delete(String id) {
        if (repairMapper.selectById(id) == null) {
            throw new BusinessException("修复申请不存在或已被删除");
        }
        repairMapper.deleteById(id);
    }
}
