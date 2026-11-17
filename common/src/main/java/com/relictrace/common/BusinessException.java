package com.relictrace.common;

import lombok.Getter;

/**
 * 业务异常。
 *
 * <p>业务层抛出的可预期异常，由全局异常处理器统一转换为 {@link Result} 返回，
 * 不会将堆栈信息泄露给客户端。</p>
 *
 * @author 李丰
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码，默认 500 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
