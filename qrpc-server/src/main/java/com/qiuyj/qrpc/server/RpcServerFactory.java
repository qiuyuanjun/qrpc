package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.service.DefaultServiceDescriptorContainer;
import com.qiuyj.qrpc.service.ServiceDescriptorContainer;
import com.qiuyj.qrpc.utils.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author qiuyj
 * @since 2020-03-01
 */
public abstract class RpcServerFactory {

    static final InternalLogger LOG = InternalLoggerFactory.getLogger(RpcServerFactory.class);

    private static final Class<?>[] RPC_SERVER_CLASS_CONSTRUCTOR_ARGS = { RpcServerConfig.class, ServiceDescriptorContainer.class };

    private RpcServerFactory() {
        // for private
    }

    /**
     * 创建默认的{@code RpcServer}实例
     */
    public static RpcServer createDefault() {
        // 1、创建RpcServerConfig对象
        RpcServerConfig serverConfig = RpcServerConfig.createDefault();
        if (LOG.isInfoEnabled()) {
            LOG.info(serverConfig.toString());
        }
        // 2、创建ServiceDescriptorContainer对象
        ServiceDescriptorContainer sdContainer = new DefaultServiceDescriptorContainer(serverConfig.isIgnoreTypeMismatch());
        // 3、创建对应的服务器
        RpcServer rpcServer = newInstance(serverConfig, sdContainer);
        // 4、配置rpcServer
        rpcServer.configure();
        return rpcServer;
    }

    @SuppressWarnings("unchecked")
    private static RpcServer newInstance(RpcServerConfig config, ServiceDescriptorContainer sdContainer) {
        Class<? extends RpcServer> serverClass = config.getRpcServerClass();
        Constructor<? extends RpcServer> serverCtor =
                (Constructor<? extends RpcServer>) ClassUtils.getConstructor(serverClass, RPC_SERVER_CLASS_CONSTRUCTOR_ARGS);
        try {
            return serverCtor.newInstance(config, sdContainer);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Error instantiating constructor", e);
        }
    }

    public static void main(String[] args) {
        createDefault();
    }

}
