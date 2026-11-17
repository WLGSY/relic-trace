package com.relictrace.common;

import com.relictrace.mapper.SysOperateLogMapper;
import com.relictrace.po.SysOperateLogDO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面。
 *
 * <p>拦截控制层的写操作（POST / PUT / DELETE），自动记录操作人、模块、类型、
 * 内容、请求参数、IP 与耗时到 {@code sys_operate_log} 表。</p>
 *
 * @author 李丰
 */
@Slf4j
@Aspect
@Component
public class OperateLogAspect {

    @Autowired
    private SysOperateLogMapper operateLogMapper;

    /** 切点：所有控制层的写操作 */
    @Pointcut("execution(* com.relictrace.controller..*(..)) "
            + "&& (@annotation(org.springframework.web.bind.annotation.PostMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.PutMapping)"
            + " || @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void writeOperation() {
    }

    @Around("writeOperation()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long cost = System.currentTimeMillis() - start;
            saveLog(joinPoint, cost);
        }
    }

    /** 异步保存操作日志（失败不影响主流程） */
    private void saveLog(ProceedingJoinPoint joinPoint, long cost) {
        try {
            LoginUser loginUser = LoginUser.get();
            if (loginUser == null) {
                return;
            }
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attrs == null ? null : attrs.getRequest();

            SysOperateLogDO operateLog = new SysOperateLogDO();
            operateLog.setOperateUserId(loginUser.getUserId());
            operateLog.setOperateUserName(loginUser.getRealName());
            // 模块名与操作类型统一走中文字典，避免出现 Auth / logout 这类英文痕迹
            String controllerName = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            String module = OperateLogDict.moduleOf(controllerName);
            String detail = OperateLogDict.detailOf(controllerName, methodName);
            operateLog.setModule(module);
            operateLog.setOperateType(OperateLogDict.typeOf(controllerName, methodName));
            operateLog.setOperateContent(loginUser.getRealName() + " " + detail);
            operateLog.setRequestParam(truncate(buildParams(joinPoint.getArgs()), 1000));
            operateLog.setIp(request == null ? "" : request.getRemoteAddr());
            operateLog.setCostTime(cost);
            operateLog.setOperateTime(LocalDateTime.now());

            operateLogMapper.insert(operateLog);
        } catch (Exception e) {
            log.warn("记录操作日志失败", e);
        }
    }

    /** 请求参数中的敏感字段（密码、令牌）在落库前做脱敏 */
    private static final java.util.regex.Pattern SENSITIVE =
            java.util.regex.Pattern.compile("(?i)\\b(password|loginPassword|oldPassword|newPassword|token|secret)\\b\\s*=\\s*[^,;)\\]}]*");

    /** 拼接方法参数（截断，避免日志过长；密码类字段脱敏） */
    private String buildParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg instanceof jakarta.servlet.ServletRequest || arg instanceof jakarta.servlet.ServletResponse) {
                continue;
            }
            String text = arg == null ? "null" : String.valueOf(arg);
            sb.append(SENSITIVE.matcher(text).replaceAll("$1=***")).append(", ");
        }
        return sb.toString();
    }

    /** 截断字符串 */
    private String truncate(String s, int maxLen) {
        if (s == null || s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen) + "...";
    }
}
