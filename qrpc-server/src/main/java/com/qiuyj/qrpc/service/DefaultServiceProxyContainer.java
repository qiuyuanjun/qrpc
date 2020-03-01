package com.qiuyj.qrpc.service;

import com.qiuyj.qrpc.QrpcException;
import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link ServiceProxyContainer}默认实现
 * @author qiuyj
 * @since 2020-02-29
 */
public class DefaultServiceProxyContainer extends AbstractServiceRegistrar implements ServiceProxyContainer {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(DefaultServiceProxyContainer.class);

    private Map<Class<?>, ServiceProxy> serviceProxyMap = new HashMap<>(32);
    private ReadWriteLock serviceProxyMapLock = new ReentrantReadWriteLock();

    @Override
    protected ServiceProxy doRegist(Class<?> interfaceClass, Object rpcService) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Class: " + interfaceClass + " is not an interface class");
        }
        ServiceProxy serviceProxy;
        serviceProxyMapLock.writeLock().lock();
        try {
            if (serviceProxyMap.containsKey(interfaceClass)) {
                throw new IllegalStateException("RPC instance object with interface name: " + interfaceClass + " already exists");
            }
            serviceProxyMap.put(interfaceClass, serviceProxy = new ServiceProxy(interfaceClass, rpcService));
        }
        finally {
            serviceProxyMapLock.writeLock().unlock();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Regist rpc service {} with interface {}", rpcService, interfaceClass);
        }
        return serviceProxy;
    }

    @Override
    public <E> ServiceProxy get(Class<? super E> interfaceClass) {
        Objects.requireNonNull(interfaceClass, "interfaceClass");
        serviceProxyMapLock.readLock().lock();
        try {
            if (!serviceProxyMap.containsKey(interfaceClass)) {
                throw new QrpcException(QrpcException.ERR_CODE_SERVICE_NOT_FOUND);
            }
            return serviceProxyMap.get(interfaceClass);
        }
        finally {
            serviceProxyMapLock.readLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E getObject(Class<? super E> interfaceClass) {
        ServiceProxy proxy = get(interfaceClass);
        return (E) proxy.getObject();
    }

    @Override
    public void clear() {
        serviceProxyMapLock.writeLock().lock();
        try {
            serviceProxyMap.clear();
        }
        finally {
            serviceProxyMapLock.writeLock().unlock();
        }
    }

}
