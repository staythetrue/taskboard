package com.demo.taskboard.common;

/**
 * 业务错误码。前三位对齐 HTTP 状态码,后两位区分细分场景。
 */
public enum ErrorCode {

    BAD_REQUEST(40000, "参数校验失败"),
    NOT_FOUND(40400, "资源不存在"),
    METHOD_NOT_ALLOWED(40500, "请求方法不支持"),
    CONFLICT(40900, "资源状态冲突"),
    INTERNAL_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
