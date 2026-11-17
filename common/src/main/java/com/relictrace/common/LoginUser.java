package com.relictrace.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 当前登录用户上下文。
 *
 * <p>认证拦截器解析 JWT 后写入 {@link ThreadLocal}，供业务层获取当前操作人信息。</p>
 *
 * @author 李丰
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 id */
    private String userId;

    /** 登录账号 */
    private String loginName;

    /** 真实姓名 */
    private String realName;

    /** 角色类型：1 系统管理员 / 2 录入员 / 3 审核员 */
    private Integer roleType;

    /** 线程上下文，绑定当前请求的登录用户 */
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /** 设置当前登录用户 */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /** 获取当前登录用户（未登录返回 null） */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 清理线程上下文 */
    public static void clear() {
        HOLDER.remove();
    }

    /** 获取当前登录用户 id（未登录返回 null） */
    public static String currentUserId() {
        LoginUser user = get();
        return user == null ? null : user.getUserId();
    }

    /** 获取当前登录用户姓名（未登录返回空串） */
    public static String currentUserName() {
        LoginUser user = get();
        return user == null ? "" : user.getRealName();
    }
}
