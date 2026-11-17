package com.relictrace.controller;

import com.relictrace.common.PageResult;
import com.relictrace.common.Result;
import com.relictrace.po.RelicRepairApplyDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 修复申请控制层。
 *
 * @author 李丰
 */
@RestController
@RequestMapping("/repairs")
public class RepairController {

    @Autowired
    private RepairService repairService;

    @GetMapping
    public Result<PageResult<RelicRepairApplyDO>> list(PageQuery query) {
        return Result.success(repairService.page(query));
    }

    @PostMapping
    public Result<String> create(@RequestBody RelicRepairApplyDO repair) {
        return Result.success(repairService.create(repair), "新增成功");
    }

    @PostMapping("/{id}/process")
    public Result<Void> process(@PathVariable String id, @RequestBody RelicRepairApplyDO repair) {
        repairService.process(id, repair);
        return Result.success(null, "处理成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        repairService.delete(id);
        return Result.success(null, "删除成功");
    }
}
