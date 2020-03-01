package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.service.DefaultServiceProxyContainer;
import com.qiuyj.qrpc.utils.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author qiuyj
 * @since 2020-03-01
 */
public abstract class RpcServerFactory {

    static final InternalLogger LOG = InternalLoggerFactory.getLogger(RpcServerFactory.class);

    private static final Class<?>[] RPC_SERVER_CLASS_CONSTRUCTOR_ARGS = { RpcServerConfig.class };

    private RpcServerFactory() {
        // for private
    }

    public static RpcServer createServer() {
        // 1、创建RpcServerConfig对象
        RpcServerConfig serverConfig = RpcServerConfig.createDefault();
        if (LOG.isDebugEnabled()) {
            LOG.debug(serverConfig.toString());
        }
        // 2、创建对应的服务器
        RpcServer rpcServer = newInstance(serverConfig);
        // 3、初始化server
        DefaultServiceProxyContainer container = new DefaultServiceProxyContainer();
        container.setIgnoreTypeMismatch(serverConfig.isIgnoreTypeMismatch());
        rpcServer.setServiceProxyContainer(container);
        return rpcServer;
    }

    @SuppressWarnings("unchecked")
    private static RpcServer newInstance(RpcServerConfig config) {
        Class<? extends RpcServer> serverClass = config.getRpcServerClass();
        Constructor<? extends RpcServer> serverCtor =
                (Constructor<? extends RpcServer>) ClassUtils.getConstructor(serverClass, RPC_SERVER_CLASS_CONSTRUCTOR_ARGS);
        try {
            return serverCtor.newInstance(config);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Error instantiating constructor", e);
        }
    }

}
