package com.qiuyj.qrpc.invoke;

/**
 * @author qiuyj
 * @since 2020-03-22
 */
public class MethodInvocationException extends Exception {

    private static final long serialVersionUID = -7752943383686520569L;

    public MethodInvocationException(String message) {
        super(message);
    }

    public MethodInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
