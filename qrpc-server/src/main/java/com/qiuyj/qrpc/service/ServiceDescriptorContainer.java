package com.qiuyj.qrpc.service;

import java.util.List;

/**
 * rpc实例对象容器，提供注册和获取rpc实例的方法
 * @author qiuyj
 * @since 2020-02-29
 */
public interface ServiceDescriptorContainer extends ServiceRegistrar {

    <E> ServiceDescriptor get(Class<? super E> interfaceClass);

    <E> E getObject(Class<? super E> interfaceClass);

    List<ServiceDescriptor> getAll();

    void clear();
}
