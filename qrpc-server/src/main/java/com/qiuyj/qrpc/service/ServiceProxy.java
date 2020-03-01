package com.qiuyj.qrpc.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qiuyj
 * @since 2020-02-29
 */
public class ServiceProxy {

    private final Object proxyObject;

    private final Class<?> interfaceClass;

    /**
     * 所有的rpc方法
     */
    private Map<String, Method> rpcMethods = new HashMap<>();

    public ServiceProxy(Class<?> interfaceClass, Object rpcService) {
        this.interfaceClass = interfaceClass;
        this.proxyObject = rpcService;
    }

    public Object getObject() {
        return proxyObject;
    }

    public Class<?> getInterface() {
        return interfaceClass;
    }
}