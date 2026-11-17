package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.relictrace.common.BusinessException;
import com.relictrace.common.JwtUtil;
import com.relictrace.common.LoginUser;
import com.relictrace.dto.LoginDTO;
import com.relictrace.mapper.SysUserMapper;
import com.relictrace.po.SysUserDO;
import com.relictrace.service.AuthService;
import com.relictrace.vo.LoginVO;
import com.relictrace.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现。
 *
 * @author 李丰
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 按登录账号查询用户
        SysUserDO user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUserDO>()
                        .eq(SysUserDO::getLoginName, loginDTO.getLoginName())
        );
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        // 校验账号是否启用
        if (user.getIsEnabled() == null || user.getIsEnabled() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        // 校验密码（BCrypt）
        if (!passwordEncoder.matches(loginDTO.getLoginPassword(), user.getLoginPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        // 更新最后登录时间（登录时无登录态，手动设置审计字段以满足 NOT NULL 约束）
        SysUserDO update = new SysUserDO();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        update.setUpdateUserId(user.getId());
        update.setUpdateUserName(user.getRealName());
        update.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(update);

        // 签发令牌
        String token = jwtUtil.generateToken(user.getId(), user.getLoginName(), user.getRealName(), user.getRoleType());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(toUserInfoVO(user));
        return loginVO;
    }

    @Override
    public UserInfoVO profile() {
        LoginUser loginUser = LoginUser.get();
        if (loginUser == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        SysUserDO user = sysUserMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        return toUserInfoVO(user);
    }

    /** 实体转用户信息展示对象 */
    private UserInfoVO toUserInfoVO(SysUserDO user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
