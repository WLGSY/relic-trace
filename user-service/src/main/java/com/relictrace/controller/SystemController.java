package com.relictrace.controller;

import com.relictrace.common.PageResult;
import com.relictrace.common.Result;
import com.relictrace.po.SysConfigDO;
import com.relictrace.po.SysDictDO;
import com.relictrace.po.SysOperateLogDO;
import com.relictrace.po.SysUserDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 系统管理控制层（用户 / 字典 / 参数 / 日志）。
 *
 * @author 李丰
 */
@RestController
public class SystemController {

    @Autowired
    private SystemService systemService;

    /* ------------------------- 用户管理 ------------------------- */

    @GetMapping("/users")
    public Result<PageResult<SysUserDO>> listUsers(PageQuery query) {
        return Result.success(systemService.pageUsers(query));
    }

    @PostMapping("/users")
    public Result<String> createUser(@RequestBody SysUserDO user) {
        return Result.success(systemService.createUser(user), "新增成功");
    }

    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable String id, @RequestBody SysUserDO user) {
        systemService.updateUser(id, user);
        return Result.success(null, "修改成功");
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable String id) {
        systemService.deleteUser(id);
        return Result.success(null, "删除成功");
    }

    @PutMapping("/users/{id}/password")
    public Result<Void> resetPassword(@PathVariable String id, @RequestBody Map<String, String> body) {
        systemService.resetPassword(id, body.get("password"));
        return Result.success(null, "密码重置成功");
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleStatus(@PathVariable String id, @RequestBody Map<String, Integer> body) {
        systemService.toggleStatus(id, body.get("isEnabled"));
        return Result.success(null, "操作成功");
    }

    /* ------------------------- 字典管理 ------------------------- */

    @GetMapping("/dicts")
    public Result<List<SysDictDO>> listDicts(@RequestParam(required = false) String dictType) {
        return Result.success(systemService.listDicts(dictType));
    }

    @PostMapping("/dicts")
    public Result<String> createDict(@RequestBody SysDictDO dict) {
        return Result.success(systemService.createDict(dict), "新增成功");
    }

    @PutMapping("/dicts/{id}")
    public Result<Void> updateDict(@PathVariable String id, @RequestBody SysDictDO dict) {
        systemService.updateDict(id, dict);
        return Result.success(null, "修改成功");
    }

    @DeleteMapping("/dicts/{id}")
    public Result<Void> deleteDict(@PathVariable String id) {
        systemService.deleteDict(id);
        return Result.success(null, "删除成功");
    }

    /* ------------------------- 参数配置 ------------------------- */

    @GetMapping("/configs")
    public Result<List<SysConfigDO>> listConfigs(@RequestParam(required = false) String keyword) {
        return Result.success(systemService.listConfigs(keyword));
    }

    @PutMapping("/configs/{id}")
    public Result<Void> updateConfig(@PathVariable String id, @RequestBody SysConfigDO config) {
        systemService.updateConfig(id, config);
        return Result.success(null, "修改成功");
    }

    /* ------------------------- 操作日志 ------------------------- */

    @GetMapping("/logs")
    public Result<PageResult<SysOperateLogDO>> listLogs(PageQuery query) {
        return Result.success(systemService.pageLogs(query));
    }

    @GetMapping("/logs/modules")
    public Result<List<String>> listLogModules() {
        return Result.success(systemService.listModules());
    }
}
