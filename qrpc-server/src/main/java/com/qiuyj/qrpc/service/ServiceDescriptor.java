package com.qiuyj.qrpc.service;

import com.qiuyj.qrpc.annotation.RpcRuntime;
import com.qiuyj.qrpc.annotation.ServiceRuntimeConfig;
import com.qiuyj.qrpc.utils.RpcRuntimeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author qiuyj
 * @since 2020-02-29
 */
public class ServiceDescriptor {

    private final Object proxyObject;

    private final Class<?> interfaceClass;

    private ServerServiceRuntimeConfig runtimeConfig;

    /**
     * 所有的rpc方法
     */
    private Map<String, RpcMethodInfo> rpcMethods = new HashMap<>();

    public ServiceDescriptor(Class<?> interfaceClass, Object rpcService) {
        this.interfaceClass = parseInterface(interfaceClass);
        this.proxyObject = parseMethodInfo(rpcService);
    }

    /**
     * 根据方法签名得到对应的方法信息
     * @param methodName 方法名
     * @param parameterTypes 方法参数类型
     * @return 对应的方法信息
     */
    public Optional<RpcMethodInfo> getMethodInfo(String methodName, Class<?>... parameterTypes) {
        String methodSig = RpcRuntimeUtils.getMethodSig(methodName, parameterTypes);
        return Optional.ofNullable(rpcMethods.get(methodSig));
    }

    private Object parseMethodInfo(Object rpcService) {
        Class<?> cls = rpcService.getClass();
        Method actualMethod;
        for (Class<?> inter = interfaceClass;
             Objects.nonNull(inter) && Object.class != inter;
             inter = inter.getSuperclass()) {
            for (Method m : inter.getDeclaredMethods()) {
                // java8及以上允许接口中有static方法，java9及以上允许接口中有private方法
                // 这两种modifier的方法都要排除
                if (Modifier.isStatic(m.getModifiers()) || Modifier.isPrivate(m.getModifiers())) {
                    continue;
                }
                try {
                    actualMethod = cls.getMethod(m.getName(), m.getParameterTypes());
                }
                catch (NoSuchMethodException e) {
                    throw new IllegalStateException(
                            "Can not find method: " + m.getName() + " with parameter types: " + Arrays.toString(m.getParameterTypes()) + " in object: " + rpcService);
                }
                ServerServiceRuntimeConfig runtimeConfig = m.isAnnotationPresent(RpcRuntime.class)
                        ? new ServerServiceRuntimeConfig(m.getAnnotation(RpcRuntime.class))
                        : new ServerServiceRuntimeConfig();
                rpcMethods.put(RpcRuntimeUtils.getMethodSig(m), new RpcMethodInfo(actualMethod, runtimeConfig));
            }
        }
        return rpcService;
    }

    private Class<?> parseInterface(Class<?> interfaceClass) {
        runtimeConfig = interfaceClass.isAnnotationPresent(RpcRuntime.class)
                ? new ServerServiceRuntimeConfig(interfaceClass.getAnnotation(RpcRuntime.class))
                : new ServerServiceRuntimeConfig();
        return interfaceClass;
    }

    public Object getObject() {
        return proxyObject;
    }

    public Class<?> getInterface() {
        return interfaceClass;
    }

    public static class ServerServiceRuntimeConfig implements ServiceRuntimeConfig {

        private int timeout;

        private String version;

        private ServerServiceRuntimeConfig() {
            // do nothing;
        }

        private ServerServiceRuntimeConfig(RpcRuntime runtime) {
            this.timeout = (int) runtime.unit().toSeconds(runtime.timeout());
            this.version = runtime.version();
        }

        @Override
        public boolean isAsync() {
            throw new UnsupportedOperationException("Server side can not support this config");
        }

        @Override
        public int getTimeout() {
            return timeout;
        }

        @Override
        public int getRetry() {
            throw new UnsupportedOperationException("Server side can not support this config");
        }

        @Override
        public boolean isCheck() {
            throw new UnsupportedOperationException("Server side can not support this config");
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getServiceUrl() {
            throw new UnsupportedOperationException("Server side can not support this config");
        }
    }

    public static class RpcMethodInfo {

        private ServerServiceRuntimeConfig runtimeConfig;

        private Method method;

        private RpcMethodInfo(Method method, ServerServiceRuntimeConfig runtimeConfig) {
            this.method = method;
            this.runtimeConfig = runtimeConfig;
        }

        public String getMethodName() {
            return method.getName();
        }

        public Class<?> getReturnType() {
            return method.getReturnType();
        }

        public Method getMethod() {
            return method;
        }

        public ServerServiceRuntimeConfig getRuntimeConfig() {
            return runtimeConfig;
        }
    }
}