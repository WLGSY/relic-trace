package com.relictrace.service;

import java.util.Map;

/**
 * 统计分析服务接口。
 *
 * @author 李丰
 */
public interface StatsService {

    /** 后台概览统计 */
    Map<String, Object> getOverview();

    /** 分类统计 */
    Map<String, Object> getCategoryStats();

    /** 年代统计 */
    Map<String, Object> getEraStats();

    /** 级别统计 */
    Map<String, Object> getLevelStats();

    /** 库房统计 */
    Map<String, Object> getWarehouseStats();

    /** 出入库趋势统计 */
    Map<String, Object> getStockTrend(Integer months);

    /** 公开统计（前台） */
    Map<String, Object> getPublicStats();
}
