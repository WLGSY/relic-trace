package com.relictrace.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应包装类。
 *
 * <p>约定：HTTP 状态码恒为 200，业务结果由 {@code code} 表达。</p>
 * <ul>
 *     <li>{@code code = 200} 成功</li>
 *     <li>{@code code = 401} 未登录 / 登录失效</li>
 *     <li>{@code code = 500} 业务异常</li>
 * </ul>
 *
 * @param <T> 业务数据类型
 * @author 李丰
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功（无数据） */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /** 成功（携带数据） */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /** 成功（自定义提示信息） */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /** 失败（默认业务异常） */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message, null);
    }

    /** 失败（自定义状态码） */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
