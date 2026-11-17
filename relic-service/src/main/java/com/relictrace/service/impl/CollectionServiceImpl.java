package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relictrace.common.BusinessException;
import com.relictrace.common.PageResult;
import com.relictrace.mapper.RelicCollectionApplyMapper;
import com.relictrace.po.RelicCollectionApplyDO;
import com.relictrace.query.CollectionQuery;
import com.relictrace.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 藏品征集申请服务实现。
 *
 * @author 李丰
 */
@Service
public class CollectionServiceImpl implements CollectionService {

    /** 征集编号前缀，完整格式：ZJ-yyyy-001 */
    private static final String APPLY_CODE_PREFIX = "ZJ-";

    /** 编号生成的最大重试次数（并发抢占编号时重试） */
    private static final int CODE_RETRY_TIMES = 3;

    @Autowired
    private RelicCollectionApplyMapper collectionApplyMapper;

    @Override
    public PageResult<RelicCollectionApplyDO> page(CollectionQuery query) {
        LambdaQueryWrapper<RelicCollectionApplyDO> wrapper = new LambdaQueryWrapper<>();
        // 关键字：藏品名称 / 征集编号
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(RelicCollectionApplyDO::getRelicName, query.getKeyword())
                    .or().like(RelicCollectionApplyDO::getApplyCode, query.getKeyword()));
        }
        // 高级查询条件
        if (query.getApplyStatus() != null) {
            wrapper.eq(RelicCollectionApplyDO::getApplyStatus, query.getApplyStatus());
        }
        if (StringUtils.hasText(query.getCategoryId())) {
            wrapper.eq(RelicCollectionApplyDO::getFkCategoryId, query.getCategoryId());
        }
        if (StringUtils.hasText(query.getEra())) {
            wrapper.eq(RelicCollectionApplyDO::getEra, query.getEra());
        }
        if (StringUtils.hasText(query.getCollectionWay())) {
            wrapper.eq(RelicCollectionApplyDO::getCollectionWay, query.getCollectionWay());
        }
        // 征集时间区间：起点取当日 00:00:00，终点取当日 23:59:59
        LocalDate begin = parseDate(query.getBeginTime());
        if (begin != null) {
            wrapper.ge(RelicCollectionApplyDO::getCollectionTime, begin.atStartOfDay());
        }
        LocalDate end = parseDate(query.getEndTime());
        if (end != null) {
            wrapper.le(RelicCollectionApplyDO::getCollectionTime, end.atTime(23, 59, 59));
        }
        wrapper.orderByDesc(RelicCollectionApplyDO::getCreateTime);

        IPage<RelicCollectionApplyDO> page = collectionApplyMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 解析 yyyy-MM-dd 日期，空值或格式非法时返回 null（不因筛选项导致查询失败） */
    private LocalDate parseDate(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public RelicCollectionApplyDO get(String id) {
        RelicCollectionApplyDO apply = collectionApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException("征集申请不存在或已被删除");
        }
        return apply;
    }

    @Override
    public String create(RelicCollectionApplyDO apply) {
        // 默认状态：草稿
        if (apply.getApplyStatus() == null) {
            apply.setApplyStatus(0);
        }
        // 征集编号由后端统一生成（apply_code 为 NOT NULL UNIQUE，前端不提供该字段）
        for (int attempt = 0; attempt < CODE_RETRY_TIMES; attempt++) {
            if (!StringUtils.hasText(apply.getApplyCode())) {
                apply.setApplyCode(generateApplyCode());
            }
            try {
                collectionApplyMapper.insert(apply);
                return apply.getId();
            } catch (DuplicateKeyException e) {
                // 并发场景下编号被其他请求抢占：清空编号与主键后重新生成
                apply.setApplyCode(null);
                apply.setId(null);
            }
        }
        throw new BusinessException("征集编号生成失败，请稍后重试");
    }

    /**
     * 生成征集编号，格式 {@code ZJ-yyyy-001}。
     *
     * <p>取当年已有编号的最大流水号 +1，不足三位左补零；
     * 首次使用时从 001 开始。</p>
     *
     * @return 新的征集编号
     */
    private String generateApplyCode() {
        String prefix = APPLY_CODE_PREFIX + LocalDate.now().getYear() + "-";
        String maxCode = collectionApplyMapper.selectMaxApplyCode(prefix);
        int seq = 1;
        if (StringUtils.hasText(maxCode) && maxCode.length() > prefix.length()) {
            try {
                seq = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                // 历史数据格式异常时退回从 001 开始，交由唯一索引兜底
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    @Override
    public void update(String id, RelicCollectionApplyDO apply) {
        RelicCollectionApplyDO exist = collectionApplyMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("征集申请不存在或已被删除");
        }
        apply.setId(id);
        // 编号与状态不允许通过编辑接口变更：编号由系统生成，状态由提交 / 审核驱动
        apply.setApplyCode(exist.getApplyCode());
        apply.setApplyStatus(exist.getApplyStatus());
        collectionApplyMapper.updateById(apply);
    }

    @Override
    public void submit(String id) {
        RelicCollectionApplyDO exist = collectionApplyMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("征集申请不存在或已被删除");
        }
        // 仅草稿（0）或审核驳回（3）可提交；待审核 / 已通过不可重复提交
        if (exist.getApplyStatus() != null && exist.getApplyStatus() != 0 && exist.getApplyStatus() != 3) {
            throw new BusinessException("当前状态不可提交审核，请刷新后重试");
        }
        RelicCollectionApplyDO update = new RelicCollectionApplyDO();
        update.setId(id);
        update.setApplyStatus(1);
        collectionApplyMapper.updateById(update);
    }

    @Override
    public void delete(String id) {
        RelicCollectionApplyDO exist = collectionApplyMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("征集申请不存在或已被删除");
        }
        collectionApplyMapper.deleteById(id);
    }

    @Override
    public void batchDelete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的记录");
        }
        collectionApplyMapper.deleteBatchIds(ids);
    }
}
