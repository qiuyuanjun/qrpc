package com.qiuyj.qrpc.message;

import com.qiuyj.qrpc.message.payload.RpcRequest;
import com.qiuyj.qrpc.message.payload.RpcResult;

import java.io.Serializable;

/**
 * rpc报文封装对象
 * @author qiuyj
 * @since 2020-03-15
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 6241553450704810609L;

    /**
     * 消息头信息
     */
    MessageHeaders messageHeaders;

    /**
     * 消息体，可以使{@link RpcRequest}对象或者是{@link RpcResult}对象
     */
    private Object messagePayload;

    public MessageHeaders getMessageHeaders() {
        return messageHeaders;
    }

    public void setMessageHeaders(MessageHeaders messageHeaders) {
        this.messageHeaders = messageHeaders;
    }

    public Object getMessagePayload() {
        return messagePayload;
    }

    public void setMessagePayload(Object messagePayload) {
        this.messagePayload = messagePayload;
    }

    public RpcRequest asRequestPayload() {
        if (!(messagePayload instanceof RpcRequest)) {
            throw new UnknownMessageTypeException("Not an RpcRequest message");
        }
        return (RpcRequest) messagePayload;
    }

    public RpcResult asResultPayload() {
        if (!(messagePayload instanceof RpcResult)) {
            throw new UnknownMessageTypeException("Not an RpcResult message");
        }
        return (RpcResult) messagePayload;
    }

}
