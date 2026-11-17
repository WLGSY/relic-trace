package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.relictrace.mapper.RelicCancelApplyMapper;
import com.relictrace.mapper.RelicCategoryMapper;
import com.relictrace.mapper.RelicCollectionApplyMapper;
import com.relictrace.mapper.RelicExhibitionLoanMapper;
import com.relictrace.mapper.RelicInfoMapper;
import com.relictrace.mapper.RelicQrCodeMapper;
import com.relictrace.mapper.RelicRepairApplyMapper;
import com.relictrace.mapper.RelicStockRecordMapper;
import com.relictrace.mapper.RelicTraceRecordMapper;
import com.relictrace.mapper.RelicWarehouseMapper;
import com.relictrace.mapper.SysUserMapper;
import com.relictrace.po.RelicCancelApplyDO;
import com.relictrace.po.RelicCategoryDO;
import com.relictrace.po.RelicCollectionApplyDO;
import com.relictrace.po.RelicExhibitionLoanDO;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.po.RelicQrCodeDO;
import com.relictrace.po.RelicRepairApplyDO;
import com.relictrace.po.RelicStockRecordDO;
import com.relictrace.po.RelicTraceRecordDO;
import com.relictrace.po.RelicWarehouseDO;
import com.relictrace.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 统计分析服务实现。
 *
 * @author 李丰
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private RelicInfoMapper relicInfoMapper;

    @Autowired
    private RelicCategoryMapper categoryMapper;

    @Autowired
    private RelicWarehouseMapper warehouseMapper;

    @Autowired
    private RelicStockRecordMapper stockRecordMapper;

    @Autowired
    private RelicCollectionApplyMapper collectionApplyMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private RelicTraceRecordMapper traceRecordMapper;

    @Autowired
    private RelicQrCodeMapper qrCodeMapper;

    @Autowired
    private RelicRepairApplyMapper repairApplyMapper;

    @Autowired
    private RelicExhibitionLoanMapper loanMapper;

    @Autowired
    private RelicCancelApplyMapper cancelApplyMapper;

    /**
     * 工作台 / 统计分析用的总览指标。
     *
     * <p>返回字段与前端 {@code WorkbenchView}、{@code StatsView} 的绑定字段
     * 严格一一对应：库存状态分布（inStock/outbound/loaned/repairing/cancelled）、
     * 本月增量（monthNew/monthStock/monthTrace）、公开数量（publicCount）、
     * 累计扫码（scanTotal）与待审核合计（pendingAudits）。</p>
     */
    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> map = new LinkedHashMap<>();
        LocalDateTime monthStart = LocalDateTime.now().toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        map.put("totalRelics", relicInfoMapper.selectCount(null));
        // 库存状态分布：1 在库 / 2 出库 / 3 借出 / 4 修复中 / 5 已注销
        map.put("inStock", countByStockStatus(1));
        map.put("outbound", countByStockStatus(2));
        map.put("loaned", countByStockStatus(3));
        map.put("repairing", countByStockStatus(4));
        map.put("cancelled", countByStockStatus(5));
        // 可被访客检索与扫码的公开藏品
        map.put("publicCount", relicInfoMapper.selectCount(
                new LambdaQueryWrapper<RelicInfoDO>().eq(RelicInfoDO::getIsPublic, 1)
        ));
        // 本月新增藏品
        map.put("monthNew", relicInfoMapper.selectCount(new LambdaQueryWrapper<RelicInfoDO>()
                .ge(RelicInfoDO::getCreateTime, monthStart)
                .lt(RelicInfoDO::getCreateTime, monthEnd)));
        // 本月出入库次数与溯源条数
        map.put("monthStock", stockRecordMapper.selectCount(new LambdaQueryWrapper<RelicStockRecordDO>()
                .ge(RelicStockRecordDO::getOperateTime, monthStart)
                .lt(RelicStockRecordDO::getOperateTime, monthEnd)));
        map.put("monthTrace", traceRecordMapper.selectCount(new LambdaQueryWrapper<RelicTraceRecordDO>()
                .ge(RelicTraceRecordDO::getTraceTime, monthStart)
                .lt(RelicTraceRecordDO::getTraceTime, monthEnd)));
        // 累计扫码次数
        map.put("scanTotal", qrCodeMapper.selectList(null).stream()
                .map(RelicQrCodeDO::getScanCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum());
        // 待审核合计，口径与审核工作台一致（各业务表状态见 AuditServiceImpl）
        long pending = collectionApplyMapper.selectCount(
                new LambdaQueryWrapper<RelicCollectionApplyDO>().eq(RelicCollectionApplyDO::getApplyStatus, 1))
                + stockRecordMapper.selectCount(
                new LambdaQueryWrapper<RelicStockRecordDO>().eq(RelicStockRecordDO::getApplyStatus, 0))
                + repairApplyMapper.selectCount(
                new LambdaQueryWrapper<RelicRepairApplyDO>().eq(RelicRepairApplyDO::getApplyStatus, 1))
                + loanMapper.selectCount(
                new LambdaQueryWrapper<RelicExhibitionLoanDO>().eq(RelicExhibitionLoanDO::getLoanStatus, 1))
                + cancelApplyMapper.selectCount(
                new LambdaQueryWrapper<RelicCancelApplyDO>().eq(RelicCancelApplyDO::getApplyStatus, 0));
        map.put("pendingAudits", pending);

        // 基础资源规模（系统管理视角，保留原有字段）
        map.put("totalWarehouses", warehouseMapper.selectCount(null));
        map.put("totalCategories", categoryMapper.selectCount(null));
        map.put("totalUsers", userMapper.selectCount(null));
        map.put("totalStocks", stockRecordMapper.selectCount(null));
        map.put("totalCollections", collectionApplyMapper.selectCount(null));
        return map;
    }

    /** 按库存状态统计藏品数量 */
    private Long countByStockStatus(Integer stockStatus) {
        return relicInfoMapper.selectCount(
                new LambdaQueryWrapper<RelicInfoDO>().eq(RelicInfoDO::getStockStatus, stockStatus)
        );
    }

    @Override
    public Map<String, Object> getCategoryStats() {
        List<RelicCategoryDO> categories = categoryMapper.selectList(null);
        List<Map<String, Object>> list = new ArrayList<>();
        for (RelicCategoryDO category : categories) {
            Long count = relicInfoMapper.selectCount(
                    new LambdaQueryWrapper<RelicInfoDO>().eq(RelicInfoDO::getFkCategoryId, category.getId())
            );
            Map<String, Object> item = new HashMap<>();
            item.put("categoryName", category.getCategoryName());
            item.put("count", count);
            list.add(item);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("list", list);
        return map;
    }

    @Override
    public Map<String, Object> getEraStats() {
        // 按年代分组统计
        List<RelicInfoDO> relics = relicInfoMapper.selectList(null);
        Map<String, Long> grouped = new LinkedHashMap<>();
        for (RelicInfoDO relic : relics) {
            String era = relic.getEra() == null ? "未知" : relic.getEra();
            grouped.merge(era, 1L, Long::sum);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        grouped.forEach((era, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("era", era);
            item.put("count", count);
            list.add(item);
        });
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("list", list);
        return map;
    }

    @Override
    public Map<String, Object> getLevelStats() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] levelNames = {"一级", "二级", "三级", "一般"};
        for (int i = 1; i <= 4; i++) {
            final int level = i;
            Long count = relicInfoMapper.selectCount(
                    new LambdaQueryWrapper<RelicInfoDO>().eq(RelicInfoDO::getRelicLevel, level)
            );
            Map<String, Object> item = new HashMap<>();
            item.put("relicLevel", level);
            item.put("levelName", levelNames[i - 1]);
            item.put("count", count);
            list.add(item);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("list", list);
        return map;
    }

    @Override
    public Map<String, Object> getWarehouseStats() {
        List<RelicWarehouseDO> warehouses = warehouseMapper.selectList(null);
        List<Map<String, Object>> list = new ArrayList<>();
        for (RelicWarehouseDO warehouse : warehouses) {
            Long count = relicInfoMapper.selectCount(
                    new LambdaQueryWrapper<RelicInfoDO>().eq(RelicInfoDO::getFkWarehouseId, warehouse.getId())
            );
            Map<String, Object> item = new HashMap<>();
            item.put("warehouseName", warehouse.getWarehouseName());
            item.put("count", count);
            list.add(item);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("list", list);
        return map;
    }

    @Override
    public Map<String, Object> getStockTrend(Integer months) {
        int n = months == null ? 6 : months;
        // 按最近 n 个月统计出入库数量
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = n - 1; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            Long inCount = stockRecordMapper.selectCount(
                    new LambdaQueryWrapper<RelicStockRecordDO>()
                            .eq(RelicStockRecordDO::getRecordType, 1)
                            .ge(RelicStockRecordDO::getOperateTime, monthStart)
                            .lt(RelicStockRecordDO::getOperateTime, monthEnd)
            );
            Long outCount = stockRecordMapper.selectCount(
                    new LambdaQueryWrapper<RelicStockRecordDO>()
                            .eq(RelicStockRecordDO::getRecordType, 2)
                            .ge(RelicStockRecordDO::getOperateTime, monthStart)
                            .lt(RelicStockRecordDO::getOperateTime, monthEnd)
            );
            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStart.format(monthFormatter));
            item.put("inCount", inCount);
            item.put("outCount", outCount);
            // 同期溯源记录数：统计分析页的「出入库与溯源趋势」第三条曲线
            item.put("traceCount", traceRecordMapper.selectCount(
                    new LambdaQueryWrapper<RelicTraceRecordDO>()
                            .ge(RelicTraceRecordDO::getTraceTime, monthStart)
                            .lt(RelicTraceRecordDO::getTraceTime, monthEnd)
            ));
            list.add(item);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("list", list);
        return map;
    }

    @Override
    public Map<String, Object> getPublicStats() {
        // 公开统计口径：只统计对访客公开、且未注销的藏品
        List<RelicInfoDO> publicRelics = relicInfoMapper.selectList(
                new LambdaQueryWrapper<RelicInfoDO>()
                        .eq(RelicInfoDO::getIsPublic, 1)
                        .ne(RelicInfoDO::getStockStatus, 5)
        );
        long totalRelics = publicRelics.size();
        List<RelicCategoryDO> categories = categoryMapper.selectList(null);
        List<RelicWarehouseDO> warehouses = warehouseMapper.selectList(null);

        // 分类构成：以分类字典为骨架，分类项与图表 name/value 字段一一对应
        List<Map<String, Object>> categoryItems = new ArrayList<>();
        for (RelicCategoryDO category : categories) {
            long count = publicRelics.stream()
                    .filter(relic -> category.getId().equals(relic.getFkCategoryId()))
                    .count();
            categoryItems.add(pair(category.getCategoryName(), count));
        }

        // 年代分布：按藏品年代分组，未知年代单列，数量倒序
        Map<String, Long> eraGrouped = new LinkedHashMap<>();
        for (RelicInfoDO relic : publicRelics) {
            String era = StringUtils.hasText(relic.getEra()) ? relic.getEra() : "未知";
            eraGrouped.merge(era, 1L, Long::sum);
        }
        List<Map<String, Object>> eraItems = eraGrouped.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(entry -> pair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 库房分布：同样以库房字典为骨架
        List<Map<String, Object>> warehouseItems = new ArrayList<>();
        for (RelicWarehouseDO warehouse : warehouses) {
            long count = publicRelics.stream()
                    .filter(relic -> warehouse.getId().equals(relic.getFkWarehouseId()))
                    .count();
            warehouseItems.add(pair(warehouse.getWarehouseName(), count));
        }

        // 累计扫码次数：汇总二维码表扫描计数
        List<RelicQrCodeDO> qrList = qrCodeMapper.selectList(null);
        int scanTotal = qrList.stream()
                .map(RelicQrCodeDO::getScanCount)
                .filter(count -> count != null)
                .mapToInt(Integer::intValue)
                .sum();

        // 最新流转动态：仅取公开藏品的最近 5 条溯源记录，避免泄露保密藏品信息
        List<String> publicIds = publicRelics.stream()
                .map(RelicInfoDO::getId)
                .collect(Collectors.toList());
        List<RelicTraceRecordDO> latestTraces = publicIds.isEmpty()
                ? new ArrayList<>()
                : traceRecordMapper.selectList(
                new LambdaQueryWrapper<RelicTraceRecordDO>()
                        .in(RelicTraceRecordDO::getFkRelicId, publicIds)
                        .orderByDesc(RelicTraceRecordDO::getTraceTime)
                        .last("limit 5")
        );
        Map<String, RelicInfoDO> relicMap = publicRelics.stream()
                .collect(Collectors.toMap(RelicInfoDO::getId, relic -> relic, (a, b) -> a));
        List<Map<String, Object>> recentTraces = new ArrayList<>();
        DateTimeFormatter traceFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (RelicTraceRecordDO trace : latestTraces) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", trace.getId());
            item.put("traceType", trace.getTraceType());
            item.put("traceTime", trace.getTraceTime() == null ? null
                    : trace.getTraceTime().format(traceFormatter));
            item.put("traceContent", trace.getTraceContent());
            item.put("operatorName", trace.getOperatorName());
            RelicInfoDO relic = relicMap.get(trace.getFkRelicId());
            if (relic != null) {
                item.put("relicName", relic.getRelicName());
                item.put("relicCode", relic.getRelicCode());
            }
            recentTraces.add(item);
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalRelics", totalRelics);
        map.put("totalCategories", (long) categories.size());
        // 前台使用：公开藏品数 / 累计扫码 / 分类、年代、库房分布 / 最新流转
        map.put("total", totalRelics);
        map.put("scanTotal", scanTotal);
        map.put("categories", categoryItems);
        map.put("eras", eraItems);
        map.put("warehouses", warehouseItems);
        map.put("recentTraces", recentTraces);
        return map;
    }

    /** 图表用的 name / value 键值对 */
    private Map<String, Object> pair(String name, Number value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}
