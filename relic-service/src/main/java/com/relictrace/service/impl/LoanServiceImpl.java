package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relictrace.common.BizCodeUtils;
import com.relictrace.common.BusinessException;
import com.relictrace.common.PageResult;
import com.relictrace.mapper.RelicExhibitionLoanMapper;
import com.relictrace.mapper.RelicInfoMapper;
import com.relictrace.po.RelicExhibitionLoanDO;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 展览借调服务实现。
 *
 * @author 李丰
 */
@Service
public class LoanServiceImpl implements LoanService {

    @Autowired
    private RelicExhibitionLoanMapper loanMapper;

    @Autowired
    private RelicInfoMapper relicInfoMapper;

    @Override
    public PageResult<RelicExhibitionLoanDO> page(PageQuery query) {
        LambdaQueryWrapper<RelicExhibitionLoanDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(RelicExhibitionLoanDO::getLoanCode, query.getKeyword())
                    .or().like(RelicExhibitionLoanDO::getLoanUnit, query.getKeyword()));
        }
        wrapper.orderByDesc(RelicExhibitionLoanDO::getCreateTime);
        IPage<RelicExhibitionLoanDO> page = loanMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public String create(RelicExhibitionLoanDO loan) {
        // 新建即进入待审核
        if (loan.getLoanStatus() == null) {
            loan.setLoanStatus(1);
        }
        // 单号由后端统一生成，避免调用方漏传导致 loan_code 唯一约束报错
        if (!StringUtils.hasText(loan.getLoanCode())) {
            String prefix = "JD-" + LocalDate.now().getYear() + "-";
            loan.setLoanCode(BizCodeUtils.nextCode(loanMapper.selectMaxLoanCode(prefix), prefix));
        }
        loanMapper.insert(loan);
        return loan.getId();
    }

    @Override
    public void handover(String id) {
        RelicExhibitionLoanDO exist = loanMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("借调记录不存在或已被删除");
        }
        RelicExhibitionLoanDO update = new RelicExhibitionLoanDO();
        update.setId(id);
        update.setLoanStatus(2);
        update.setLoanTime(LocalDateTime.now());
        loanMapper.updateById(update);
    }

    @Override
    public void returnLoan(String id, RelicExhibitionLoanDO loan) {
        RelicExhibitionLoanDO exist = loanMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("借调记录不存在或已被删除");
        }
        loan.setId(id);
        loan.setLoanStatus(3);
        loan.setActualReturnTime(LocalDateTime.now());
        loanMapper.updateById(loan);

        // 归还后藏品回到「在库」，否则工作台借出数量会一直累积
        if (exist.getFkRelicId() != null) {
            RelicInfoDO relic = new RelicInfoDO();
            relic.setId(exist.getFkRelicId());
            relic.setStockStatus(1);
            relicInfoMapper.updateById(relic);
        }
    }
}
