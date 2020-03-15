package com.qiuyj.qrpc.service;

/**
 * @author qiuyj
 * @since 2020-03-15
 */
class RegisterException extends RuntimeException {

    private Class<?> failedRegisteredInterfaceClass;

    RegisterException(Class<?> failedRegisteredInterfaceClass, String errorMessage) {
        super(errorMessage);
        this.failedRegisteredInterfaceClass = failedRegisteredInterfaceClass;
    }

    Class<?> getFailedRegisteredInterfaceClass() {
        return failedRegisteredInterfaceClass;
    }
}
