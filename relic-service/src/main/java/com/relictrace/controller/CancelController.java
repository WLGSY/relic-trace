package com.relictrace.controller;

import com.relictrace.common.PageResult;
import com.relictrace.common.Result;
import com.relictrace.po.RelicCancelApplyDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.CancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注销申请控制层。
 *
 * @author 李丰
 */
@RestController
@RequestMapping("/cancels")
public class CancelController {

    @Autowired
    private CancelService cancelService;

    @GetMapping
    public Result<PageResult<RelicCancelApplyDO>> list(PageQuery query) {
        return Result.success(cancelService.page(query));
    }

    @PostMapping
    public Result<String> create(@RequestBody RelicCancelApplyDO cancel) {
        return Result.success(cancelService.create(cancel), "新增成功");
    }

    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable String id) {
        cancelService.archive(id);
        return Result.success(null, "归档成功");
    }
}
