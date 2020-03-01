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

    <E> Optional<ServiceProxy> regist(E rpcService);

    <E> Optional<ServiceProxy> regist(Class<? super E> interfaceClass, E rpcService);

    <E> List<ServiceProxy> registAll(Collection<?> rpcServices);

    <E> List<ServiceProxy> registAll(Map<Class<?>, ?> rpcServices);
}
