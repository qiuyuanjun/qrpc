package com.qiuyj.qrpc.service;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link ServiceDescriptorContainer}默认实现
 * @author qiuyj
 * @since 2020-02-29
 */
public class DefaultServiceDescriptorContainer extends AbstractServiceRegistrar implements ServiceDescriptorContainer {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(DefaultServiceDescriptorContainer.class);

    private ConcurrentMap<Class<?>, ServiceDescriptor> serviceDescriptorMap = new ConcurrentHashMap<>(32);

    private static class ServiceDescriptorHolder {

        private ServiceDescriptor serviceDescriptor;
    }

    public DefaultServiceDescriptorContainer(boolean ignoreTypeMismatch) {
        super(ignoreTypeMismatch);
    }

    @Override
    protected ServiceDescriptor doRegister(Class<?> interfaceClass, Object rpcService) {
        if (!interfaceClass.isInterface()) {
            throw new RegisterException(interfaceClass, "Class: " + interfaceClass + " is not an interface class");
        }
        ServiceDescriptorHolder holder = new ServiceDescriptorHolder();
        serviceDescriptorMap.computeIfAbsent(interfaceClass, k -> {
            holder.serviceDescriptor = new ServiceDescriptor(k, rpcService);
            return holder.serviceDescriptor;
        });
        if (Objects.isNull(holder.serviceDescriptor)) {
            throw new RegisterException(interfaceClass, "RPC instance object with interface name: " + interfaceClass + " already exists");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Regist rpc service {} with interface {}", rpcService, interfaceClass);
        }
        return holder.serviceDescriptor;
    }

    @Override
    protected void doUnregister(Class<?> interfaceClass) {
        serviceDescriptorMap.remove(interfaceClass);
    }

    @Override
    public <E> Optional<ServiceDescriptor> get(Class<? super E> interfaceClass) {
        Objects.requireNonNull(interfaceClass, "interfaceClass");
        return Optional.ofNullable(serviceDescriptorMap.get(interfaceClass));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Optional<E> getObject(Class<? super E> interfaceClass) {
        return get(interfaceClass).map(sd -> (E) sd.getObject());
    }

    @Override
    public List<ServiceDescriptor> getAll() {
        return new ArrayList<>(serviceDescriptorMap.values());
    }

    @Override
    public void clear() {
        serviceDescriptorMap.clear();
    }

}
