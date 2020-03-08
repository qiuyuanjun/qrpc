package com.qiuyj.qrpc.service;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
        /*return rpcServices.stream()
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
                        (l, e) -> l.add(doRegister((Class<?>) e.getKey(), e.getValue())),
                        ArrayList::addAll);*/
        return doRegisterAll(rpcServices.stream()
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
        // 检测所有的类型是否匹配
        rpcServices.forEach(this::checkType);
        /*return rpcServices.entrySet()
                .stream()
                .collect(ArrayList::new,
                        (l, e) -> register((Class<? super E>) e.getKey(), (E) e.getValue()).ifPresent(l::add),
                        ArrayList::addAll);*/
        return doRegisterAll(rpcServices);
    }

    /**
     * 具体的注册方法，交给子类实现
     * @param interfaceClass rpc接口
     * @param rpcService rpc服务实例对象
     */
    protected abstract ServiceDescriptor doRegister(Class<?> interfaceClass, Object rpcService);

    protected abstract List<ServiceDescriptor> doRegisterAll(Map<Class<?>, ?> rpcServices);

    @Override
    public boolean unregister(ServiceDescriptor serviceDescriptor) {
        doUnregister(serviceDescriptor.getInterface());
        return true;
    }

    @Override
    public boolean unregisterAll(List<ServiceDescriptor> serviceDescriptors) {
        List<Class<?>> keys = serviceDescriptors.stream()
                .map(ServiceDescriptor::getInterface)
                .collect(Collectors.toList());
        doUnregisterAll(keys);
        return true;
    }

    /**
     * 具体的注销方法，交给子类实现
     * @param interfaceClass rpc接口
     */
    protected abstract void doUnregister(Class<?> interfaceClass);

    protected abstract void doUnregisterAll(List<Class<?>> interfaceClasses);
}
