package com.qiuyj.qrpc.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 服务注册，用于注册所有rpc实例对象
 * @author qiuyj
 * @since 2020-02-29
 */
@SuppressWarnings("unused")
public interface ServiceRegistrar {

    <E> Optional<ServiceDescriptor> register(E rpcService);

    <E> Optional<ServiceDescriptor> register(Class<? super E> interfaceClass, E rpcService);

    <E> List<ServiceDescriptor> registerAll(Collection<?> rpcServices);

    <E> List<ServiceDescriptor> registerAll(Map<Class<?>, ?> rpcServices);

    boolean unregister(ServiceDescriptor serviceDescriptor);

    boolean unregisterAll(List<ServiceDescriptor> serviceDescriptors);
}
