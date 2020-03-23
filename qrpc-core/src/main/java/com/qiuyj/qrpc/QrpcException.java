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
     * 需要检查的异常未处理
     */
    public static final int ERR_CODE_CAUGHT_EXCEPTION_UNHANDLER = 2;

    /**
     * 未知的消息类型
     */
    public static final int ERR_CODE_UNKNOWN_MESSAGE_TYPE = 3;

    /**
     * 消息有问题，不符合定义的消息
     */
    public static final int ERR_CODE_BAD_MESSAGE = 4;

    /**
     * 消息序列化异常
     */
    public static final int ERR_CODE_MESSAGE_SERIALIZE = 5;

    /**
     * 消息反序列化异常
     */
    public static final int ERR_CODE_MESSAGE_DESERIALIZE = 5;

    /**
     * 执行目标方法报错
     */
    public static final int ERR_CODE_INVOKE_TARGET = 6;

  /**
     * 错误码
     */
    private final int errorCode;

    public QrpcException(int errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public QrpcException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }
}
