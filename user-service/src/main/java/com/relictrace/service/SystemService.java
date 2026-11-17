package com.relictrace.service;

import com.relictrace.common.PageResult;
import com.relictrace.po.SysConfigDO;
import com.relictrace.po.SysDictDO;
import com.relictrace.po.SysOperateLogDO;
import com.relictrace.po.SysUserDO;
import com.relictrace.query.PageQuery;

import java.util.List;

/**
 * 系统管理服务接口（用户 / 字典 / 参数 / 日志）。
 *
 * @author 李丰
 */
public interface SystemService {

    /* ------------------------- 用户管理 ------------------------- */

    /** 分页查询用户 */
    PageResult<SysUserDO> pageUsers(PageQuery query);

    /** 新增用户 */
    String createUser(SysUserDO user);

    /** 修改用户 */
    void updateUser(String id, SysUserDO user);

    /** 删除用户 */
    void deleteUser(String id);

    /** 重置用户密码 */
    void resetPassword(String id, String password);

    /** 启用 / 禁用用户 */
    void toggleStatus(String id, Integer isEnabled);

    /* ------------------------- 字典管理 ------------------------- */

    /** 查询字典列表 */
    List<SysDictDO> listDicts(String dictType);

    /** 新增字典项 */
    String createDict(SysDictDO dict);

    /** 修改字典项 */
    void updateDict(String id, SysDictDO dict);

    /** 删除字典项 */
    void deleteDict(String id);

    /* ------------------------- 参数配置 ------------------------- */

    /** 查询参数配置 */
    List<SysConfigDO> listConfigs(String keyword);

    /** 修改参数配置 */
    void updateConfig(String id, SysConfigDO config);

    /* ------------------------- 操作日志 ------------------------- */

    /** 分页查询操作日志 */
    PageResult<SysOperateLogDO> pageLogs(PageQuery query);

    /** 查询日志模块列表 */
    List<String> listModules();
}
