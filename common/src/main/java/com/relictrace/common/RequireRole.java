package com.relictrace.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色访问控制注解。
 *
 * <p>标注在控制器类或方法上，声明允许访问的角色类型集合，
 * 由 {@link RoleInterceptor} 在进入方法前校验当前登录用户的 {@code roleType}。
 * 未标注的接口不做角色限制（仍受登录校验保护）。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * &#64;RequireRole({1, 3})   // 仅系统管理员、审核员可访问
 * &#64;PostMapping("/audits")
 * </pre>
 *
 * @author 李丰
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许访问的角色类型：1 系统管理员 / 2 录入员 / 3 审核员 */
    int[] value();

    /** 无权限时的提示信息 */
    String message() default "当前角色无权执行该操作";
}
