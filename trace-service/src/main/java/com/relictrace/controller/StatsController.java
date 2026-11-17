package com.relictrace.controller;

import com.relictrace.common.Result;
import com.relictrace.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计分析控制层。
 *
 * @author 李丰
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(statsService.getOverview());
    }

    @GetMapping("/category")
    public Result<Map<String, Object>> category() {
        return Result.success(statsService.getCategoryStats());
    }

    @GetMapping("/era")
    public Result<Map<String, Object>> era() {
        return Result.success(statsService.getEraStats());
    }

    @GetMapping("/level")
    public Result<Map<String, Object>> level() {
        return Result.success(statsService.getLevelStats());
    }

    @GetMapping("/warehouse")
    public Result<Map<String, Object>> warehouse() {
        return Result.success(statsService.getWarehouseStats());
    }

    @GetMapping("/stock-trend")
    public Result<Map<String, Object>> stockTrend(@RequestParam(defaultValue = "6") Integer months) {
        return Result.success(statsService.getStockTrend(months));
    }

    @GetMapping("/public")
    public Result<Map<String, Object>> publicStats() {
        return Result.success(statsService.getPublicStats());
    }
}
