package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relictrace.common.BizCodeUtils;
import com.relictrace.common.BusinessException;
import com.relictrace.common.LoginUser;
import com.relictrace.common.PageResult;
import com.relictrace.mapper.RelicCancelApplyMapper;
import com.relictrace.mapper.RelicInfoMapper;
import com.relictrace.po.RelicCancelApplyDO;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.CancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 注销申请服务实现。
 *
 * @author 李丰
 */
@Service
public class CancelServiceImpl implements CancelService {

    @Autowired
    private RelicCancelApplyMapper cancelMapper;

    @Autowired
    private RelicInfoMapper relicInfoMapper;

    @Override
    public PageResult<RelicCancelApplyDO> page(PageQuery query) {
        LambdaQueryWrapper<RelicCancelApplyDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(RelicCancelApplyDO::getApplyCode, query.getKeyword());
        }
        wrapper.orderByDesc(RelicCancelApplyDO::getCreateTime);
        IPage<RelicCancelApplyDO> page = cancelMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public String create(RelicCancelApplyDO cancel) {
        // 新建即进入待审核
        if (cancel.getApplyStatus() == null) {
            cancel.setApplyStatus(0);
        }
        // 单号由后端统一生成，避免调用方漏传导致 apply_code 唯一约束报错
        if (!StringUtils.hasText(cancel.getApplyCode())) {
            String prefix = "ZX-" + LocalDate.now().getYear() + "-";
            cancel.setApplyCode(BizCodeUtils.nextCode(cancelMapper.selectMaxApplyCode(prefix), prefix));
        }
        cancelMapper.insert(cancel);
        return cancel.getId();
    }

    @Override
    public void archive(String id) {
        RelicCancelApplyDO exist = cancelMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("注销申请不存在或已被删除");
        }
        // 归档：填写归档人、归档时间，并将藏品状态置为已注销
        RelicCancelApplyDO update = new RelicCancelApplyDO();
        update.setId(id);
        update.setArchiveUserName(LoginUser.currentUserName());
        update.setArchiveTime(LocalDateTime.now());
        cancelMapper.updateById(update);

        // 更新藏品库存状态为已注销
        RelicInfoDO relic = new RelicInfoDO();
        relic.setId(exist.getFkRelicId());
        relic.setStockStatus(5);
        relicInfoMapper.updateById(relic);
    }
}
