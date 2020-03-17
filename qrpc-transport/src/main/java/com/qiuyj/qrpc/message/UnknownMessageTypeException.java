package com.qiuyj.qrpc.message;

import com.qiuyj.qrpc.QrpcException;

/**
 * @author qiuyj
 * @since 2020-03-16
 */
public class UnknownMessageTypeException extends QrpcException {

    private static final long serialVersionUID = 8722149535815985790L;

    public UnknownMessageTypeException(String message) {
        super(QrpcException.ERR_CODE_UNKNOWN_MESSAGE_TYPE, message);
    }
}
