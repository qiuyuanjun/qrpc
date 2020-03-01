package com.qiuyj.qrpc.service;

/**
 * rpc实例对象容器，提供注册和获取rpc实例的方法
 * @author qiuyj
 * @since 2020-02-29
 */
public interface ServiceProxyContainer extends ServiceRegistrar {

    <E> ServiceProxy get(Class<? super E> interfaceClass);

    <E> E getObject(Class<? super E> interfaceClass);

    void clear();
}
