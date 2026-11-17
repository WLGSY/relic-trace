package com.relictrace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求参数。
 *
 * @author 李丰
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 登录账号 */
    @NotBlank(message = "登录账号不能为空")
    private String loginName;

    /** 登录密码 */
    @NotBlank(message = "登录密码不能为空")
    private String loginPassword;
}
