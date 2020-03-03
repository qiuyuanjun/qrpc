package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.server.RpcServer;
import com.qiuyj.qrpc.server.RpcServerConfig;
import com.qiuyj.qrpc.service.ServiceDescriptorContainer;
import io.netty.bootstrap.ServerBootstrap;

/**
 * 基于netty的rpc服务器的实现
 * @author qiuyj
 * @since 2020-02-29
 */
public class NettyRpcServer extends RpcServer {

    private ServerBootstrap serverBootstrap;

    public NettyRpcServer(RpcServerConfig config, ServiceDescriptorContainer serviceDescriptorContainer) {
        super(config, serviceDescriptorContainer);
    }

    @Override
    protected void internalStart(RpcServerConfig config) {

    }

    @Override
    protected void internalShutdown() {

    }
}
