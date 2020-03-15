package com.qiuyj.qrpc.cnxn;

import java.io.IOException;

/**
 * 代表一个从客户端到服务器端的连接
 * @author qiuyj
 * @since 2020-03-05
 */
public interface RpcConnection {

    /**
     * 发送消息
     * @param message 消息内容
     */
    void sendMessage(Object message) throws IOException;

    /**
     * 当前连接是否打开
     * @return {@code true}如果连接还是打开状态，{@code false}当前连接已经关闭
     */
    boolean isOpen();

    /**
     * 关闭当前连接
     */
    void close() throws IOException;

}
