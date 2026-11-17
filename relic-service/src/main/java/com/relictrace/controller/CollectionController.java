package com.relictrace.controller;

import com.relictrace.common.PageResult;
import com.relictrace.common.Result;
import com.relictrace.po.RelicCollectionApplyDO;
import com.relictrace.query.CollectionQuery;
import com.relictrace.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 藏品征集申请控制层。
 *
 * @author 李丰
 */
@RestController
@RequestMapping("/collection-applies")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @GetMapping
    public Result<PageResult<RelicCollectionApplyDO>> list(CollectionQuery query) {
        return Result.success(collectionService.page(query));
    }

    @GetMapping("/{id}")
    public Result<RelicCollectionApplyDO> get(@PathVariable String id) {
        return Result.success(collectionService.get(id));
    }

    @PostMapping
    public Result<String> create(@RequestBody RelicCollectionApplyDO apply) {
        return Result.success(collectionService.create(apply), "新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id, @RequestBody RelicCollectionApplyDO apply) {
        collectionService.update(id, apply);
        return Result.success(null, "修改成功");
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable String id) {
        collectionService.submit(id);
        return Result.success(null, "提交成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        collectionService.delete(id);
        return Result.success(null, "删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody Map<String, List<String>> body) {
        collectionService.batchDelete(body.get("ids"));
        return Result.success(null, "批量删除成功");
    }
}
