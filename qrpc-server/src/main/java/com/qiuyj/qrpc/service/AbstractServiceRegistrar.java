package com.qiuyj.qrpc.service;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author qiuyj
 * @since 2020-03-01
 */
@SuppressWarnings({"unchecked", "unused"})
public abstract class AbstractServiceRegistrar implements ServiceRegistrar {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(AbstractServiceRegistrar.class);

    private boolean ignoreTypeMismatch;

    public void setIgnoreTypeMismatch(boolean ignoreTypeMismatch) {
        this.ignoreTypeMismatch = ignoreTypeMismatch;
    }

    private static Class<?> getRpcInterface(Object o) {
        Class<?> interfaceClass = getRpcInterface(o.getClass());
        if (Objects.isNull(interfaceClass)) {
            throw new IllegalArgumentException(
                    "Unable to determine the interface type implemented by the current RPC instance object: " + o);
        }
        return interfaceClass;
    }

    private static Class<?> getRpcInterface(Class<?> c) {
        Class<?>[] interfaces = c.getInterfaces();
        int len = interfaces.length;
        if (len == 0) {
            for (Class<?> superClass = c.getSuperclass();
                 Objects.nonNull(superClass) && Object.class != superClass;
                 superClass = superClass.getSuperclass()) {
                Class<?> inter = getRpcInterface(superClass);
                if (Objects.nonNull(inter)) {
                    return inter;
                }
            }
            return null;
        }
        else {
            return len > 1 ? null : interfaces[0];
        }
    }

    private Class<?> checkType(Class<?> c, Object o) {
        if (!c.isInstance(o)) {
            if (!ignoreTypeMismatch) {
                throw new IllegalArgumentException(
                        "Type mismatch, rpc service: " + o + " is not an instance of interface: " + c);
            }
            // 如果是忽略类型不匹配的情况，那么直接返回null
            return null;
        }
        return c;
    }

    @Override
    public <E> Optional<ServiceProxy> regist(E rpcService) {
        return regist((Class<? super E>) getRpcInterface(rpcService), rpcService);
    }

    @Override
    public <E> Optional<ServiceProxy> regist(Class<? super E> interfaceClass, E rpcService) {
        ServiceProxy serviceProxy = null;
        if (Objects.isNull(checkType(interfaceClass, rpcService))) {
            // 忽略类型不匹配的情况，那么就记录日志，并且忽略当前的注册
            LOG.warn("Type mismatch, rpc service: {} is not an instance of interface: {}, and ignore regist",
                    rpcService, interfaceClass);
        }
        else {
            serviceProxy = doRegist(interfaceClass, rpcService);
        }
        return Optional.ofNullable(serviceProxy);
    }

    @Override
    public <E> List<ServiceProxy> registAll(Collection<?> rpcServices) {
        return rpcServices.stream()
                .collect(HashMap::new, (m, o) -> {
                    Class<?> rpcInterface = getRpcInterface(o);
                    if (Objects.isNull(checkType(rpcInterface, o))) {
                        // 忽略类型不匹配的情况，那么就记录日志，并且直接返回
                        LOG.warn("Type mismatch, rpc service: {} is not an instance of interface: {}, and ignore regist",
                                o, rpcInterface);
                    }
                    else {
                        m.put(rpcInterface, o);
                    }
                }, HashMap::putAll)
                .entrySet()
                .stream()
                .collect(ArrayList::new,
                        (l, e) -> l.add(doRegist((Class<?>) e.getKey(), e.getValue())),
                        ArrayList::addAll);
    }

    @Override
    public <E> List<ServiceProxy> registAll(Map<Class<?>, ?> rpcServices) {
        if (!ignoreTypeMismatch) {
            // 如果ignoreTypeMismatch为true，那么注册之前，要先对所有的进行类型检查
            rpcServices.forEach(this::checkType);
        }
        // 依次调用regist方法，如果遇到对象实例没有实现给定的接口，那么忽略注册
        return rpcServices.entrySet()
                .stream()
                .collect(ArrayList::new,
                        (l, e) -> regist((Class<? super E>) e.getKey(), (E) e.getValue()).ifPresent(l::add),
                        ArrayList::addAll);
    }

    /**
     * 具体的注册方法，交给子类实现
     * @param interfaceClass rpc接口
     * @param rpcService rpc服务实例对象
     */
    protected abstract ServiceProxy doRegist(Class<?> interfaceClass, Object rpcService);
}
