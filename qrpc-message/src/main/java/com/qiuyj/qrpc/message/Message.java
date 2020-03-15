package com.qiuyj.qrpc.message;

import java.io.Serializable;
import java.util.Map;

/**
 * rpc报文封装对象
 * @author qiuyj
 * @since 2020-03-15
 */
public class Message implements Serializable {

    /**
     * 消息头信息
     */
    private MessageHeader header;

    /**
     * 请求对象，当messageType为REQUEST_TYPE的时候有值
     */
    private RpcRequest requestMessage;

    /**
     * 结果相应对象，当messageType为RESULT_TYPE的时候有值
     */
    private RpcResult resultMessage;

    /**
     * 附加信息对象
     */
    private Map<Object, Object> attachment;

    public enum MessageType {

        REQUEST_TYPE,

        RESULT_TYPE
    }
}
