package com.qiuyj.qrpc.service;

import com.qiuyj.qrpc.QrpcException;
import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link ServiceDescriptorContainer}默认实现
 * @author qiuyj
 * @since 2020-02-29
 */
public class DefaultServiceDescriptorContainer extends AbstractServiceRegistrar implements ServiceDescriptorContainer {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(DefaultServiceDescriptorContainer.class);

    private Map<Class<?>, ServiceDescriptor> serviceDescriptorMap = new HashMap<>(32);
    private ReadWriteLock serviceDescriptorMapLock = new ReentrantReadWriteLock();

    public DefaultServiceDescriptorContainer(boolean ignoreTypeMismatch) {
        super(ignoreTypeMismatch);
    }

    @Override
    protected ServiceDescriptor doRegist(Class<?> interfaceClass, Object rpcService) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Class: " + interfaceClass + " is not an interface class");
        }
        ServiceDescriptor serviceDescriptor;
        serviceDescriptorMapLock.writeLock().lock();
        try {
            if (serviceDescriptorMap.containsKey(interfaceClass)) {
                throw new IllegalStateException("RPC instance object with interface name: " + interfaceClass + " already exists");
            }
            serviceDescriptorMap.put(interfaceClass, serviceDescriptor = new ServiceDescriptor(interfaceClass, rpcService));
        }
        finally {
            serviceDescriptorMapLock.writeLock().unlock();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Regist rpc service {} with interface {}", rpcService, interfaceClass);
        }
        return serviceDescriptor;
    }

    @Override
    public <E> ServiceDescriptor get(Class<? super E> interfaceClass) {
        Objects.requireNonNull(interfaceClass, "interfaceClass");
        serviceDescriptorMapLock.readLock().lock();
        try {
            if (!serviceDescriptorMap.containsKey(interfaceClass)) {
                throw new QrpcException(QrpcException.ERR_CODE_SERVICE_NOT_FOUND);
            }
            return serviceDescriptorMap.get(interfaceClass);
        }
        finally {
            serviceDescriptorMapLock.readLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E getObject(Class<? super E> interfaceClass) {
        ServiceDescriptor proxy = get(interfaceClass);
        return (E) proxy.getObject();
    }

    @Override
    public List<ServiceDescriptor> getAll() {
        serviceDescriptorMapLock.readLock().lock();
        try {
            return new ArrayList<>(serviceDescriptorMap.values());
        }
        finally {
            serviceDescriptorMapLock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        serviceDescriptorMapLock.writeLock().lock();
        try {
            serviceDescriptorMap.clear();
        }
        finally {
            serviceDescriptorMapLock.writeLock().unlock();
        }
    }

}
