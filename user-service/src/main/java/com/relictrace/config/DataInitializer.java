package com.relictrace.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.relictrace.mapper.SysUserMapper;
import com.relictrace.po.SysUserDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 默认账号初始化器。
 *
 * <p>应用启动时若 {@code sys_user} 表中不存在 admin 账号，则自动创建默认管理员，
 * 密码由 {@link PasswordEncoder} 现场 BCrypt 加密，确保与登录校验逻辑一致。</p>
 *
 * @author 李丰
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createIfAbsent("u-admin-0001", "admin", "系统管理员", 1);
        createIfAbsent("u-operator-0002", "zhangly", "张录入", 2);
        createIfAbsent("u-auditor-0003", "lishen", "李审核", 3);
    }

    /** 账号不存在时创建演示账号，密码统一 123456 */
    private void createIfAbsent(String id, String loginName, String realName, int roleType) {
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUserDO>().eq(SysUserDO::getLoginName, loginName)
        );
        if (count != null && count > 0) {
            return;
        }
        SysUserDO user = new SysUserDO();
        user.setId(id);
        user.setLoginName(loginName);
        user.setLoginPassword(passwordEncoder.encode("123456"));
        user.setRealName(realName);
        user.setRoleType(roleType);
        user.setIsEnabled(1);
        user.setCreateUserId("system");
        user.setCreateUserName("系统");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateUserId("system");
        user.setUpdateUserName("系统");
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);
        user.setVersion(0);
        sysUserMapper.insert(user);
        log.info("已初始化演示账号：{} / 123456", loginName);
    }
}
