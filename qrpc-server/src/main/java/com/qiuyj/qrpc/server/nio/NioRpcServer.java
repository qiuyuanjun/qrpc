package com.qiuyj.qrpc.server.nio;

import com.qiuyj.qrpc.server.RpcServer;
import com.qiuyj.qrpc.server.RpcServerConfig;

/**
 * 基于nio的rpc服务器的实现
 * @author qiuyj
 * @since 2020-02-29
 */
public class NioRpcServer extends RpcServer {

    public NioRpcServer(RpcServerConfig config) {
        super(config);
    }

    @Override
    protected void internalStart(RpcServerConfig config) {

    }

    @Override
    protected void internalShutdown() {

    }
}
