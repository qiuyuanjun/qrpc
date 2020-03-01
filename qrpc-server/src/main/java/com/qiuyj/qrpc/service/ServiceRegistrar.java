package com.qiuyj.qrpc.service;

import java.util.Collection;
import java.util.Map;

/**
 * 服务注册，用于注册所有rpc实例对象
 * @author qiuyj
 * @since 2020-02-29
 */
@SuppressWarnings("unused")
public interface ServiceRegistrar {

    <E> void regist(E rpcService);

    <E> void regist(Class<? super E> interfaceClass, E rpcService);

    <E> void registAll(Collection<?> rpcServices);

    <E> void registAll(Map<Class<?>, ?> rpcServices);
}
