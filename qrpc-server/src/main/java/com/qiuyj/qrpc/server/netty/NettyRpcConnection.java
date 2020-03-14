package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.cnxn.RpcConnection;

import java.io.IOException;

/**
 * 基于netty的客户端到服务器端之间的连接实现
 * @author qiuyj
 * @since 2020-03-05
 */
public class NettyRpcConnection implements RpcConnection {

    @Override
    public void sendMessage(Object message) {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
}
