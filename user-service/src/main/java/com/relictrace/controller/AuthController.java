package com.relictrace.controller;

import com.relictrace.common.Result;
import com.relictrace.dto.LoginDTO;
import com.relictrace.service.AuthService;
import com.relictrace.vo.LoginVO;
import com.relictrace.vo.UserInfoVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制层。
 *
 * <p>提供登录、退出、个人资料接口。</p>
 *
 * @author 李丰
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO), "登录成功");
    }

    /**
     * 退出登录（无状态 JWT，服务端无需额外处理，前端清除本地令牌即可）。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null, "已退出登录");
    }

    /**
     * 获取当前登录用户资料。
     */
    @GetMapping("/profile")
    public Result<UserInfoVO> profile() {
        return Result.success(authService.profile());
    }
}
