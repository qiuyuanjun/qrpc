package com.qiuyj.qrpc.service;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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

    protected AbstractServiceRegistrar() {
        // do nothing
    }

    protected AbstractServiceRegistrar(boolean ignoreTypeMismatch) {
        this.ignoreTypeMismatch = ignoreTypeMismatch;
    }

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
    public <E> Optional<ServiceDescriptor> register(E rpcService) {
        return register((Class<? super E>) getRpcInterface(rpcService), rpcService);
    }

    @Override
    public <E> Optional<ServiceDescriptor> register(Class<? super E> interfaceClass, E rpcService) {
        ServiceDescriptor serviceDescriptor = null;
        if (Objects.isNull(checkType(interfaceClass, rpcService))) {
            // 忽略类型不匹配的情况，那么就记录日志，并且忽略当前的注册
            LOG.warn("Type mismatch, rpc service: {} is not an instance of interface: {}, and ignore regist",
                    rpcService, interfaceClass);
        }
        else {
            serviceDescriptor = doRegister(interfaceClass, rpcService);
        }
        return Optional.ofNullable(serviceDescriptor);
    }

    @Override
    public <E> List<ServiceDescriptor> registerAll(Collection<?> rpcServices) {
        return commonRegistAll(rpcServices.stream()
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
                }, HashMap::putAll));
    }

    @Override
    public <E> List<ServiceDescriptor> registerAll(Map<Class<?>, ?> rpcServices) {
        // 检测所有的类型是否匹配，如果ignoreTypeMismatch为true的话，那么过滤掉类型不匹配的
        return commonRegistAll(rpcServices.entrySet()
                .stream()
                .filter(me -> Objects.nonNull(checkType(me.getKey(), me.getValue())))
                .collect(HashMap::new,
                        (m, me) -> m.put(me.getKey(), me.getValue()),
                        HashMap::putAll));
    }

    private List<ServiceDescriptor> commonRegistAll(Map<Class<?>, ?> checkedServices) {
        LinkedList<Class<?>> registered = new LinkedList<>();
        try {
            return checkedServices.entrySet()
                    .stream()
                    .collect(ArrayList::new,
                            (l, e) -> {
                                registered.addFirst(e.getKey());
                                l.add(doRegister(e.getKey(), e.getValue()));
                            },
                            ArrayList::addAll);
        }
        catch (RegisterException e) {
            // 可能会抛出异常，那么抛出异常，需要注销掉所有已经注册的服务
            Class<?> failedRegister = registered.removeFirst();
            assert failedRegister == e.getFailedRegisteredInterfaceClass();
            registered.forEach(this::doUnregister);
            throw e;
        }
    }

    /**
     * 具体的注册方法，交给子类实现
     * @param interfaceClass rpc接口
     * @param rpcService rpc服务实例对象
     */
    protected abstract ServiceDescriptor doRegister(Class<?> interfaceClass, Object rpcService);

    @Override
    public boolean unregister(ServiceDescriptor serviceDescriptor) {
        doUnregister(serviceDescriptor.getInterface());
        return true;
    }

    @Override
    public boolean unregisterAll(List<ServiceDescriptor> serviceDescriptors) {
        serviceDescriptors.forEach(this::unregister);
        return true;
    }

    /**
     * 具体的注销方法，交给子类实现
     * @param interfaceClass rpc接口
     */
    protected abstract void doUnregister(Class<?> interfaceClass);
}
