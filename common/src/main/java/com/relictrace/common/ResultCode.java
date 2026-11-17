package com.relictrace.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务状态码枚举。
 *
 * @author 李丰
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 未登录 / 登录失效 */
    UNAUTHORIZED(401, "登录状态已失效，请重新登录"),

    /** 无权限 */
    FORBIDDEN(403, "无权限访问"),

    /** 业务异常 */
    ERROR(500, "操作失败"),

    /** 资源 / 接口不存在 */
    NOT_FOUND(404, "请求的资源不存在"),

    /** 参数校验失败 */
    BAD_REQUEST(400, "参数错误");

    private final Integer code;
    private final String message;
}
