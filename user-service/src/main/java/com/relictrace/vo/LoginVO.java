package com.relictrace.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应展示对象。
 *
 * <p>与前端约定一致：{@code token} 为访问令牌，{@code user} 为当前用户信息。</p>
 *
 * @author 李丰
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问令牌 */
    private String token;

    /** 用户信息 */
    private UserInfoVO user;
}
