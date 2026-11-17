package com.relictrace.service;

import com.relictrace.dto.LoginDTO;
import com.relictrace.vo.LoginVO;
import com.relictrace.vo.UserInfoVO;

/**
 * 认证服务接口。
 *
 * @author 李丰
 */
public interface AuthService {

    /**
     * 用户登录。
     *
     * @param loginDTO 登录参数
     * @return 登录结果（令牌 + 用户信息）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户资料。
     *
     * @return 用户信息
     */
    UserInfoVO profile();
}
