package com.relictrace.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

/**
 * 角色鉴权拦截器。
 *
 * <p>读取控制器方法（或类）上的 {@link RequireRole} 注解，与当前登录用户的
 * {@code roleType} 比对；不匹配时直接输出 403 业务响应，不进入业务方法。</p>
 *
 * <p>必须在 {@link AuthInterceptor} 之后执行——本拦截器依赖其写入的
 * {@link LoginUser} 上下文。</p>
 *
 * @author 李丰
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            // 静态资源、错误页等非控制器方法直接放行
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return true;
        }

        LoginUser user = LoginUser.get();
        if (user == null) {
            write(response, ResultCode.UNAUTHORIZED.getCode(), "未登录或登录已失效");
            return false;
        }

        int[] allowed = requireRole.value();
        Integer roleType = user.getRoleType();
        boolean pass = roleType != null
                && Arrays.stream(allowed).anyMatch(item -> item == roleType);
        if (!pass) {
            write(response, ResultCode.FORBIDDEN.getCode(), requireRole.message());
            return false;
        }
        return true;
    }

    /** 输出业务响应（HTTP 200 + 业务状态码，与前端约定一致） */
    private void write(HttpServletResponse response, Integer code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}
