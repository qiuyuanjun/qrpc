package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.Lifecycle;
import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.service.ServiceProxy;
import com.qiuyj.qrpc.service.ServiceProxyContainer;
import com.qiuyj.qrpc.service.ServiceRegistrar;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * rpc服务器，接收所有的rpc客户端请求，并将所处理的结果返回给rpc客户端
 * @author qiuyj
 * @since 2020-02-29
 */
public abstract class RpcServer implements Lifecycle, ServiceRegistrar {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(RpcServer.class);

    private RpcServerConfig config;

    private ServiceProxyContainer serviceProxyContainer;

    protected RpcServer(RpcServerConfig config) {
        this.config = config;
    }

    public void setServiceProxyContainer(ServiceProxyContainer serviceProxyContainer) {
        this.serviceProxyContainer = serviceProxyContainer;
    }

    //--------------------------------Lifecycle

    @Override
    public void start() {
        // 1、初始化所有的过滤器
        // 2、将所有注册的服务暴露到服务注册中心（如果支持服务注册中心）
        // 3、启动socket服务器
        internalStart(config);
    }

    protected abstract void internalStart(RpcServerConfig config);

    @Override
    public void shutdown() {
        internalShutdown();

        if (Objects.nonNull(serviceProxyContainer)) {
            serviceProxyContainer.clear();
        }
    }

    protected abstract void internalShutdown();

    //--------------------------------ServiceRegistrar

    @Override
    public <E> Optional<ServiceProxy> regist(E rpcService) {
        if (Objects.isNull(rpcService)) {
            throw new NullPointerException("rpcService");
        }
        return serviceProxyContainer.regist(rpcService);
    }

    @Override
    public <E> Optional<ServiceProxy> regist(Class<? super E> interfaceClass, E rpcService) {
        if (Objects.isNull(interfaceClass)) {
            return regist(rpcService);
        }
        else if (Objects.isNull(rpcService)) {
            throw new NullPointerException("rpcService");
        }
        else {
            return serviceProxyContainer.regist(interfaceClass, rpcService);
        }
    }

    @Override
    public <E> List<ServiceProxy> registAll(Collection<?> rpcServices) {
        if (Objects.isNull(rpcServices)) {
            throw new NullPointerException("rpcServices");
        }
        List<ServiceProxy> serviceProxies;
        if (rpcServices.isEmpty()) {
            serviceProxies = List.of();
            LOG.warn("Empty rpcServiecs and do nothing");
        }
        else {
            serviceProxies = serviceProxyContainer.<E>registAll(rpcServices);
        }
        return serviceProxies;
    }

    @Override
    public <E> List<ServiceProxy> registAll(Map<Class<?>, ?> rpcServices) {
        if (Objects.isNull(rpcServices)) {
            throw new NullPointerException("rpcServices");
        }
        List<ServiceProxy> serviceProxies;
        if (rpcServices.isEmpty()) {
            serviceProxies = Collections.emptyList();
            LOG.warn("Empty rpcServiecs and do nothing");
        }
        else {
            serviceProxies = serviceProxyContainer.<E>registAll(rpcServices);
        }
        return serviceProxies;
    }
}
