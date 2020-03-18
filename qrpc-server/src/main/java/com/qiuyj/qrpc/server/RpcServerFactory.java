package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.message.MessageConverters;
import com.qiuyj.qrpc.service.DefaultServiceDescriptorContainer;
import com.qiuyj.qrpc.service.ServiceDescriptorContainer;
import com.qiuyj.qrpc.utils.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2020-03-01
 */
public abstract class RpcServerFactory {

    static final InternalLogger LOG = InternalLoggerFactory.getLogger(RpcServerFactory.class);

    private static final Class<?>[] RPC_SERVER_CLASS_CONSTRUCTOR_ARGS = { RpcServerConfig.class, ServiceDescriptorContainer.class };

    /**
     * 共享的消息转换器，当一个jvm进程同时包含rpc服务器和客户端的时候，那么客户端和服务器共享消息转换器
     */
    public static volatile MessageConverters sharedMessageConverters;

    public RpcServerFactory() {
        // empty
    }

    /**
     * 创建rpc服务器实例
     */
    public abstract RpcServer newInstance();

    /**
     * 创建默认的{@link DefaultRpcServerFactory}对象
     */
    public static RpcServerFactory defaultFactory() {
        return new DefaultRpcServerFactory();
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
        // 4、设置消息转换器
        rpcServer.setMessageConverters(getOrCreateMessageConverters());
        // 5、配置rpcServer
        rpcServer.configure(serverConfig);
        return rpcServer;
    }

    private static MessageConverters getOrCreateMessageConverters() {
        if (Objects.nonNull(sharedMessageConverters)) {
            return sharedMessageConverters;
        }
        Class<?> clientFactoryClass = ClassUtils.resolveClass("com.qiuyj.qrpc.client.RpcClientFactory");
        if (Objects.isNull(clientFactoryClass)) {
            // 如果当前环境找不到{@code com.qiuyj.qrpc.client.RpcClientFactory}类的话，那么表明不存在客户端和服务器端同时存在的情况
            return sharedMessageConverters = new MessageConverters();
        }
        // 找到sharedMessageConverters属性，看是否有值，如果有值，那么直接返回
        Field f = ClassUtils.getDeclaredField(clientFactoryClass, "sharedMessageConverters");
        try {
            sharedMessageConverters = (MessageConverters) f.get(null);
        }
        catch (IllegalAccessException e) {
            // ignore, never happen
        }
        if (Objects.isNull(sharedMessageConverters)) {
            sharedMessageConverters = new MessageConverters();
        }
        return sharedMessageConverters;
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

    private static class DefaultRpcServerFactory extends RpcServerFactory {

        @Override
        public RpcServer newInstance() {
            return createDefault();
        }
    }

}
