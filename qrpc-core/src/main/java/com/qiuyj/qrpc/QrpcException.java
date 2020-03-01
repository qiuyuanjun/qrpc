package com.qiuyj.qrpc;

/**
 * qrpc全局统一异常父类
 * @author qiuyj
 * @since 2020-02-29
 */
public class QrpcException extends RuntimeException {

    /**
     * 服务未找到
     */
    public static final int ERR_CODE_SERVICE_NOT_FOUND = 1;

    /**
     * 错误码
     */
    private final int errorCode;

    public QrpcException(int errorCode) {
        this.errorCode = errorCode;
    }

    public QrpcException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }
}
