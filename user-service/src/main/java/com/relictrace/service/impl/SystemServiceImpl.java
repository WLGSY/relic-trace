package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relictrace.common.BusinessException;
import com.relictrace.common.LoginUser;
import com.relictrace.common.PageResult;
import com.relictrace.mapper.SysConfigMapper;
import com.relictrace.mapper.SysDictMapper;
import com.relictrace.mapper.SysOperateLogMapper;
import com.relictrace.mapper.SysUserMapper;
import com.relictrace.po.SysConfigDO;
import com.relictrace.po.SysDictDO;
import com.relictrace.po.SysOperateLogDO;
import com.relictrace.po.SysUserDO;
import com.relictrace.query.PageQuery;
import com.relictrace.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统管理服务实现。
 *
 * @author 李丰
 */
@Service
public class SystemServiceImpl implements SystemService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysDictMapper dictMapper;

    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private SysOperateLogMapper operateLogMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* ------------------------- 用户管理 ------------------------- */

    @Override
    public PageResult<SysUserDO> pageUsers(PageQuery query) {
        LambdaQueryWrapper<SysUserDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUserDO::getLoginName, query.getKeyword())
                    .or().like(SysUserDO::getRealName, query.getKeyword()));
        }
        wrapper.orderByAsc(SysUserDO::getCreateTime);
        IPage<SysUserDO> page = userMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public String createUser(SysUserDO user) {
        // 登录账号唯一性校验
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUserDO>().eq(SysUserDO::getLoginName, user.getLoginName())
        );
        if (count != null && count > 0) {
            throw new BusinessException("登录账号已存在：" + user.getLoginName());
        }
        // 密码加密存储
        if (StringUtils.hasText(user.getLoginPassword())) {
            user.setLoginPassword(passwordEncoder.encode(user.getLoginPassword()));
        }
        if (user.getIsEnabled() == null) {
            user.setIsEnabled(1);
        }
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public void updateUser(String id, SysUserDO user) {
        if (userMapper.selectById(id) == null) {
            throw new BusinessException("用户不存在或已被删除");
        }
        user.setId(id);
        // 密码字段不在此处更新（由 resetPassword 单独处理）
        user.setLoginPassword(null);
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(String id) {
        if (userMapper.selectById(id) == null) {
            throw new BusinessException("用户不存在或已被删除");
        }
        if (id.equals(LoginUser.currentUserId())) {
            throw new BusinessException("不能删除当前登录用户");
        }
        userMapper.deleteById(id);
    }

    @Override
    public void resetPassword(String id, String password) {
        if (userMapper.selectById(id) == null) {
            throw new BusinessException("用户不存在或已被删除");
        }
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("密码不能为空");
        }
        SysUserDO update = new SysUserDO();
        update.setId(id);
        update.setLoginPassword(passwordEncoder.encode(password));
        userMapper.updateById(update);
    }

    @Override
    public void toggleStatus(String id, Integer isEnabled) {
        if (userMapper.selectById(id) == null) {
            throw new BusinessException("用户不存在或已被删除");
        }
        SysUserDO update = new SysUserDO();
        update.setId(id);
        update.setIsEnabled(isEnabled);
        userMapper.updateById(update);
    }

    /* ------------------------- 字典管理 ------------------------- */

    @Override
    public List<SysDictDO> listDicts(String dictType) {
        LambdaQueryWrapper<SysDictDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dictType)) {
            wrapper.eq(SysDictDO::getDictType, dictType);
        }
        wrapper.orderByAsc(SysDictDO::getDictType).orderByAsc(SysDictDO::getSortNo);
        return dictMapper.selectList(wrapper);
    }

    @Override
    public String createDict(SysDictDO dict) {
        if (dict.getIsEnabled() == null) {
            dict.setIsEnabled(1);
        }
        dictMapper.insert(dict);
        return dict.getId();
    }

    @Override
    public void updateDict(String id, SysDictDO dict) {
        if (dictMapper.selectById(id) == null) {
            throw new BusinessException("字典项不存在或已被删除");
        }
        dict.setId(id);
        dictMapper.updateById(dict);
    }

    @Override
    public void deleteDict(String id) {
        if (dictMapper.selectById(id) == null) {
            throw new BusinessException("字典项不存在或已被删除");
        }
        dictMapper.deleteById(id);
    }

    /* ------------------------- 参数配置 ------------------------- */

    @Override
    public List<SysConfigDO> listConfigs(String keyword) {
        LambdaQueryWrapper<SysConfigDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysConfigDO::getConfigKey, keyword)
                    .or().like(SysConfigDO::getConfigName, keyword));
        }
        wrapper.orderByAsc(SysConfigDO::getConfigKey);
        return configMapper.selectList(wrapper);
    }

    @Override
    public void updateConfig(String id, SysConfigDO config) {
        if (configMapper.selectById(id) == null) {
            throw new BusinessException("参数配置不存在或已被删除");
        }
        config.setId(id);
        configMapper.updateById(config);
    }

    /* ------------------------- 操作日志 ------------------------- */

    @Override
    public PageResult<SysOperateLogDO> pageLogs(PageQuery query) {
        LambdaQueryWrapper<SysOperateLogDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysOperateLogDO::getOperateUserName, query.getKeyword())
                    .or().like(SysOperateLogDO::getOperateContent, query.getKeyword()));
        }
        wrapper.orderByDesc(SysOperateLogDO::getOperateTime);
        IPage<SysOperateLogDO> page = operateLogMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<String> listModules() {
        List<SysOperateLogDO> logs = operateLogMapper.selectList(
                new LambdaQueryWrapper<SysOperateLogDO>().select(SysOperateLogDO::getModule)
        );
        return logs.stream().map(SysOperateLogDO::getModule).distinct().collect(Collectors.toList());
    }
}
