package com.qiuyj.qrpc.message;

import com.qiuyj.qrpc.QrpcException;

/**
 * @author qiuyj
 * @since 2020-03-16
 */
public class BadMessageException extends QrpcException {

    private static final long serialVersionUID = 2802081869765018030L;

    public BadMessageException(String cause) {
        super(QrpcException.ERR_CODE_BAD_MESSAGE, cause);
    }
}
