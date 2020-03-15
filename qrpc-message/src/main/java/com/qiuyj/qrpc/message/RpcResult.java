package com.qiuyj.qrpc.message;

import java.io.Serializable;

/**
 * @author qiuyj
 * @since 2020-03-15
 */
public class RpcResult implements Serializable {

    private String requestId;

    private Throwable cause;

    private Object result;
}
