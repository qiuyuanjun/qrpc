package com.qiuyj.qrpc.message;

/**
 * rpc报文封装对象
 * @author qiuyj
 * @since 2020-03-15
 */
public class RpcMessage<T> {

    public enum MessageType {

        REQUEST_TYPE,

        RESULT_TYPE
    }
}
