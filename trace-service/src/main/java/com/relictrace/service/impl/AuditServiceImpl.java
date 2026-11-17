package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relictrace.common.BusinessException;
import com.relictrace.common.LoginUser;
import com.relictrace.common.PageResult;
import com.relictrace.common.ResultCode;
import com.relictrace.dto.AuditDTO;
import com.relictrace.mapper.RelicCancelApplyMapper;
import com.relictrace.mapper.RelicCollectionApplyMapper;
import com.relictrace.mapper.RelicExhibitionLoanMapper;
import com.relictrace.mapper.RelicInfoMapper;
import com.relictrace.mapper.RelicRepairApplyMapper;
import com.relictrace.mapper.RelicStockRecordMapper;
import com.relictrace.mapper.SysAuditRecordMapper;
import com.relictrace.po.RelicCancelApplyDO;
import com.relictrace.po.RelicCollectionApplyDO;
import com.relictrace.po.RelicExhibitionLoanDO;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.po.RelicRepairApplyDO;
import com.relictrace.po.RelicStockRecordDO;
import com.relictrace.po.SysAuditRecordDO;
import com.relictrace.query.AuditQuery;
import com.relictrace.query.PageQuery;
import com.relictrace.service.AuditService;
import com.relictrace.vo.AuditItemVO;
import com.relictrace.vo.PendingCountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审核服务实现。
 *
 * <p>统一处理征集、出入库、修复、借调、注销五类审核，
 * 审核结果同步回写对应业务表状态，并记录审核日志。</p>
 *
 * @author 李丰
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private RelicCollectionApplyMapper collectionApplyMapper;

    @Autowired
    private RelicStockRecordMapper stockRecordMapper;

    @Autowired
    private RelicRepairApplyMapper repairMapper;

    @Autowired
    private RelicExhibitionLoanMapper loanMapper;

    @Autowired
    private RelicCancelApplyMapper cancelMapper;

    @Autowired
    private RelicInfoMapper relicInfoMapper;

    @Autowired
    private SysAuditRecordMapper auditRecordMapper;

    /** 业务类型常量 */
    private static final int BIZ_COLLECTION = 1;
    private static final int BIZ_STOCK = 2;
    private static final int BIZ_REPAIR = 3;
    private static final int BIZ_LOAN = 4;
    private static final int BIZ_CANCEL = 5;

    @Override
    public PageResult<AuditItemVO> listPending(AuditQuery query) {
        // 汇总各类待审核项
        List<AuditItemVO> all = new ArrayList<>();
        all.addAll(pendingCollections());
        all.addAll(pendingStocks());
        all.addAll(pendingRepairs());
        all.addAll(pendingLoans());
        all.addAll(pendingCancels());

        // 按业务类型过滤（工作台顶部五类卡片切换）
        if (query.getBizType() != null) {
            all.removeIf(item -> !query.getBizType().equals(item.getBizType()));
        }

        // 按提交时间倒序
        all.sort(Comparator.comparing(AuditItemVO::getSubmitTime, Comparator.nullsLast(Comparator.reverseOrder())));

        // 内存分页
        int pageNum = query.getPageNum() == null ? 1 : query.getPageNum().intValue();
        int pageSize = query.getPageSize() == null ? 10 : query.getPageSize().intValue();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(all.size(), from + pageSize);
        List<AuditItemVO> list = from >= all.size() ? new ArrayList<>() : all.subList(from, to);
        return new PageResult<>(list, (long) all.size(), (long) pageNum, (long) pageSize);
    }

    @Override
    public PendingCountVO getPendingCount() {
        PendingCountVO vo = new PendingCountVO();
        vo.setCollection(countCollection());
        vo.setStock(countStock());
        vo.setRepair(countRepair());
        vo.setLoan(countLoan());
        vo.setCancel(countCancel());
        vo.setTotal(vo.getCollection() + vo.getStock() + vo.getRepair() + vo.getLoan() + vo.getCancel());
        return vo;
    }

    @Override
    public PageResult<SysAuditRecordDO> listAuditRecords(PageQuery query) {
        LambdaQueryWrapper<SysAuditRecordDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysAuditRecordDO::getAuditTime);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysAuditRecordDO::getBizCode, query.getKeyword())
                    .or().like(SysAuditRecordDO::getAuditUserName, query.getKeyword()));
        }
        IPage<SysAuditRecordDO> page = auditRecordMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        // 补齐业务类型名称，避免审核记录列表「业务类型」列空白
        page.getRecords().forEach(record -> record.setBizTypeName(bizTypeName(record.getBizType())));
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 校验单据是否仍处于待审核状态。
     *
     * <p>审核通过或驳回后单据已离开待办列表，若再次提交审核会覆盖原结论、
     * 并留下两条相互矛盾的审核记录，因此在此统一拦截。</p>
     */
    private void assertAuditable(Integer bizType, String bizId) {
        if (bizType == null || bizId == null) {
            throw new BusinessException("审核对象不完整");
        }
        boolean pending = switch (bizType) {
            case BIZ_COLLECTION -> {
                RelicCollectionApplyDO apply = collectionApplyMapper.selectById(bizId);
                yield apply != null && Integer.valueOf(1).equals(apply.getApplyStatus());
            }
            case BIZ_STOCK -> {
                RelicStockRecordDO record = stockRecordMapper.selectById(bizId);
                yield record != null && Integer.valueOf(0).equals(record.getApplyStatus());
            }
            case BIZ_REPAIR -> {
                RelicRepairApplyDO apply = repairMapper.selectById(bizId);
                yield apply != null && Integer.valueOf(1).equals(apply.getApplyStatus());
            }
            case BIZ_LOAN -> {
                RelicExhibitionLoanDO loan = loanMapper.selectById(bizId);
                yield loan != null && Integer.valueOf(1).equals(loan.getLoanStatus());
            }
            case BIZ_CANCEL -> {
                RelicCancelApplyDO apply = cancelMapper.selectById(bizId);
                yield apply != null && Integer.valueOf(0).equals(apply.getApplyStatus());
            }
            default -> throw new BusinessException("未知的业务类型");
        };
        if (!pending) {
            throw new BusinessException("该单据不在待审核状态，不能重复审核");
        }
    }

    /** 业务类型编码 → 中文名称 */
    private String bizTypeName(Integer bizType) {
        if (bizType == null) {
            return "";
        }
        return switch (bizType) {
            case BIZ_COLLECTION -> "征集审核";
            case BIZ_STOCK -> "出入库审核";
            case BIZ_REPAIR -> "修复审核";
            case BIZ_LOAN -> "借调审核";
            case BIZ_CANCEL -> "注销审核";
            default -> "其他";
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(AuditDTO auditDTO) {
        assertAuditor();
        if (auditDTO.getAuditResult() == null || auditDTO.getAuditResult() == 2) {
            if (auditDTO.getAuditOpinion() == null || auditDTO.getAuditOpinion().trim().isEmpty()) {
                throw new BusinessException("驳回时必须填写审核意见");
            }
        }
        doAudit(auditDTO.getBizType(), auditDTO.getFkBizId(), auditDTO.getBizCode(),
                auditDTO.getAuditResult(), auditDTO.getAuditOpinion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAudit(List<Map<String, Object>> items, String auditOpinion) {
        assertAuditor();
        if (items == null || items.isEmpty()) {
            throw new BusinessException("请选择要审核的记录");
        }
        for (Map<String, Object> item : items) {
            Integer bizType = item.get("bizType") == null ? null : Integer.valueOf(String.valueOf(item.get("bizType")));
            // 兼容两种字段名：待办列表返回 bizId，历史调用传 fkBizId
            Object rawId = item.get("fkBizId") != null ? item.get("fkBizId") : item.get("bizId");
            String bizId = rawId == null ? null : String.valueOf(rawId);
            String bizCode = item.get("bizCode") == null ? null : String.valueOf(item.get("bizCode"));
            doAudit(bizType, bizId, bizCode, 1, auditOpinion);
        }
    }

    /**
     * 审核角色校验：仅系统管理员（1）与审核员（3）可执行审核。
     *
     * <p>控制器已通过 {@code @RequireRole} 拦截，这里再兜一层，
     * 防止绕过 Web 层直接调用服务方法。</p>
     */
    private void assertAuditor() {
        LoginUser user = LoginUser.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未登录或登录已失效");
        }
        Integer roleType = user.getRoleType();
        if (roleType == null || (roleType != 1 && roleType != 3)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "当前角色无权执行审核操作");
        }
    }

    /**
     * 执行单条审核，回写业务表状态并记录审核日志。
     */
    private void doAudit(Integer bizType, String bizId, String bizCode, Integer auditResult, String auditOpinion) {
        // 只允许审核处于「待审核」状态的单据，防止重复审核覆盖既有结论
        assertAuditable(bizType, bizId);

        // 记录审核日志
        SysAuditRecordDO record = new SysAuditRecordDO();
        record.setBizType(bizType);
        record.setFkBizId(bizId);
        record.setBizCode(bizCode);
        record.setAuditUserId(LoginUser.currentUserId());
        record.setAuditUserName(LoginUser.currentUserName());
        record.setAuditTime(LocalDateTime.now());
        record.setAuditResult(auditResult);
        record.setAuditOpinion(auditOpinion);
        record.setCreateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        auditRecordMapper.insert(record);

        // 回写业务表状态
        switch (bizType) {
            case BIZ_COLLECTION -> auditCollection(bizId, auditResult, auditOpinion);
            case BIZ_STOCK -> auditStock(bizId, auditResult, auditOpinion);
            case BIZ_REPAIR -> auditRepair(bizId, auditResult, auditOpinion);
            case BIZ_LOAN -> auditLoan(bizId, auditResult, auditOpinion);
            case BIZ_CANCEL -> auditCancel(bizId, auditResult, auditOpinion);
            default -> throw new BusinessException("未知的业务类型");
        }
    }

    /** 征集审核：2 通过 / 3 驳回 */
    private void auditCollection(String bizId, Integer result, String opinion) {
        RelicCollectionApplyDO apply = new RelicCollectionApplyDO();
        apply.setId(bizId);
        apply.setApplyStatus(result == 1 ? 2 : 3);
        apply.setAuditOpinion(opinion);
        collectionApplyMapper.updateById(apply);
    }

    /** 出入库审核：1 通过 / 2 驳回 */
    private void auditStock(String bizId, Integer result, String opinion) {
        RelicStockRecordDO exist = stockRecordMapper.selectById(bizId);
        RelicStockRecordDO record = new RelicStockRecordDO();
        record.setId(bizId);
        record.setApplyStatus(result == 1 ? 1 : 2);
        record.setAuditOpinion(opinion);
        stockRecordMapper.updateById(record);
        if (exist == null || result != 1) {
            return;
        }
        // 审核通过后联动藏品库存状态：入库/移库回到在库，出库置为出库
        int targetStatus = exist.getRecordType() != null && exist.getRecordType() == 2 ? 2 : 1;
        updateRelicStockStatus(exist.getFkRelicId(), targetStatus, null);
    }

    /** 修复审核：2 通过 / 3 驳回 */
    private void auditRepair(String bizId, Integer result, String opinion) {
        RelicRepairApplyDO exist = repairMapper.selectById(bizId);
        RelicRepairApplyDO apply = new RelicRepairApplyDO();
        apply.setId(bizId);
        apply.setApplyStatus(result == 1 ? 2 : 3);
        apply.setAuditOpinion(opinion);
        repairMapper.updateById(apply);
        if (exist != null && result == 1) {
            // 修复通过：藏品进入「修复中」
            updateRelicStockStatus(exist.getFkRelicId(), 4, null);
        }
    }

    /** 借调审核：2 借出中 / 4 驳回 */
    private void auditLoan(String bizId, Integer result, String opinion) {
        RelicExhibitionLoanDO exist = loanMapper.selectById(bizId);
        RelicExhibitionLoanDO loan = new RelicExhibitionLoanDO();
        loan.setId(bizId);
        loan.setLoanStatus(result == 1 ? 2 : 4);
        loan.setAuditOpinion(opinion);
        loanMapper.updateById(loan);
        if (exist != null && result == 1) {
            // 借调通过：藏品置为「借出」
            updateRelicStockStatus(exist.getFkRelicId(), 3, null);
        }
    }

    /** 注销审核：1 通过 / 2 驳回 */
    private void auditCancel(String bizId, Integer result, String opinion) {
        RelicCancelApplyDO exist = cancelMapper.selectById(bizId);
        RelicCancelApplyDO cancel = new RelicCancelApplyDO();
        cancel.setId(bizId);
        cancel.setApplyStatus(result == 1 ? 1 : 2);
        cancel.setAuditOpinion(opinion);
        cancelMapper.updateById(cancel);
        if (exist != null && result == 1) {
            // 注销通过：藏品置为「已注销」，同时撤下公开展示
            updateRelicStockStatus(exist.getFkRelicId(), 5, 0);
        }
    }

    /**
     * 联动更新藏品库存状态。
     *
     * <p>审核通过后若只回写业务单状态而不改藏品状态，工作台「馆藏概览」的
     * 在库/出库/借出/修复中/已注销分布会长期停留在旧值，与实际业务脱节。</p>
     *
     * @param relicId     藏品 id
     * @param stockStatus 目标库存状态：1 在库 / 2 出库 / 3 借出 / 4 修复中 / 5 已注销
     * @param isPublic    是否公开，传 null 表示不修改
     */
    private void updateRelicStockStatus(String relicId, int stockStatus, Integer isPublic) {
        if (relicId == null || relicId.isEmpty()) {
            return;
        }
        RelicInfoDO relic = new RelicInfoDO();
        relic.setId(relicId);
        relic.setStockStatus(stockStatus);
        if (isPublic != null) {
            relic.setIsPublic(isPublic);
        }
        relicInfoMapper.updateById(relic);
    }

    /* ------------------------- 待审核列表构建 ------------------------- */

    private List<AuditItemVO> pendingCollections() {
        List<RelicCollectionApplyDO> list = collectionApplyMapper.selectList(
                new LambdaQueryWrapper<RelicCollectionApplyDO>().eq(RelicCollectionApplyDO::getApplyStatus, 1)
        );
        return list.stream().map(a -> {
            AuditItemVO vo = new AuditItemVO();
            vo.setBizType(BIZ_COLLECTION);
            vo.setFkBizId(a.getId());
            vo.setBizCode(a.getApplyCode());
            vo.setTitle(a.getRelicName());
            vo.setSubmitUser(a.getCreateUserName());
            vo.setSubmitTime(a.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private List<AuditItemVO> pendingStocks() {
        List<RelicStockRecordDO> list = stockRecordMapper.selectList(
                new LambdaQueryWrapper<RelicStockRecordDO>().eq(RelicStockRecordDO::getApplyStatus, 0)
        );
        return list.stream().map(a -> {
            AuditItemVO vo = new AuditItemVO();
            vo.setBizType(BIZ_STOCK);
            vo.setFkBizId(a.getId());
            vo.setBizCode(a.getRecordCode());
            vo.setTitle(a.getSourceTarget() == null ? a.getRecordCode() : a.getSourceTarget());
            vo.setSubmitUser(a.getOperatorName());
            vo.setSubmitTime(a.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private List<AuditItemVO> pendingRepairs() {
        List<RelicRepairApplyDO> list = repairMapper.selectList(
                new LambdaQueryWrapper<RelicRepairApplyDO>().eq(RelicRepairApplyDO::getApplyStatus, 1)
        );
        return list.stream().map(a -> {
            AuditItemVO vo = new AuditItemVO();
            vo.setBizType(BIZ_REPAIR);
            vo.setFkBizId(a.getId());
            vo.setBizCode(a.getApplyCode());
            vo.setTitle(a.getRepairItem());
            vo.setSubmitUser(a.getCreateUserName());
            vo.setSubmitTime(a.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private List<AuditItemVO> pendingLoans() {
        List<RelicExhibitionLoanDO> list = loanMapper.selectList(
                new LambdaQueryWrapper<RelicExhibitionLoanDO>().eq(RelicExhibitionLoanDO::getLoanStatus, 1)
        );
        return list.stream().map(a -> {
            AuditItemVO vo = new AuditItemVO();
            vo.setBizType(BIZ_LOAN);
            vo.setFkBizId(a.getId());
            vo.setBizCode(a.getLoanCode());
            vo.setTitle(a.getLoanUnit());
            vo.setSubmitUser(a.getCreateUserName());
            vo.setSubmitTime(a.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private List<AuditItemVO> pendingCancels() {
        List<RelicCancelApplyDO> list = cancelMapper.selectList(
                new LambdaQueryWrapper<RelicCancelApplyDO>().eq(RelicCancelApplyDO::getApplyStatus, 0)
        );
        return list.stream().map(a -> {
            AuditItemVO vo = new AuditItemVO();
            vo.setBizType(BIZ_CANCEL);
            vo.setFkBizId(a.getId());
            vo.setBizCode(a.getApplyCode());
            vo.setTitle(a.getCancelReason());
            vo.setSubmitUser(a.getCancelUserName());
            vo.setSubmitTime(a.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /* ------------------------- 待审核数量统计 ------------------------- */

    private Long countCollection() {
        return collectionApplyMapper.selectCount(
                new LambdaQueryWrapper<RelicCollectionApplyDO>().eq(RelicCollectionApplyDO::getApplyStatus, 1)
        );
    }

    private Long countStock() {
        return stockRecordMapper.selectCount(
                new LambdaQueryWrapper<RelicStockRecordDO>().eq(RelicStockRecordDO::getApplyStatus, 0)
        );
    }

    private Long countRepair() {
        return repairMapper.selectCount(
                new LambdaQueryWrapper<RelicRepairApplyDO>().eq(RelicRepairApplyDO::getApplyStatus, 1)
        );
    }

    private Long countLoan() {
        return loanMapper.selectCount(
                new LambdaQueryWrapper<RelicExhibitionLoanDO>().eq(RelicExhibitionLoanDO::getLoanStatus, 1)
        );
    }

    private Long countCancel() {
        return cancelMapper.selectCount(
                new LambdaQueryWrapper<RelicCancelApplyDO>().eq(RelicCancelApplyDO::getApplyStatus, 0)
        );
    }
}
