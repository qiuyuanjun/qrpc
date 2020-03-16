package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.QrpcThread;
import com.qiuyj.qrpc.cnxn.RpcConnection;
import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.service.ServiceDescriptor;
import com.qiuyj.qrpc.service.ServiceDescriptorContainer;
import com.qiuyj.qrpc.service.ServiceRegistrar;
import com.qiuyj.qrpc.utils.Partition;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * rpc服务器，接收所有的rpc客户端请求，并将所处理的结果返回给rpc客户端
 * @author qiuyj
 * @since 2020-02-29
 */
public abstract class RpcServer implements Lifecycle, ServiceRegistrar {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(RpcServer.class);

    private BlockingQueue<RegistrationUnregistrationInfo> asyncServiceRegistrationUnregistrationQueue;

    private RpcServerConfig config;

    private ServiceDescriptorContainer serviceDescriptorContainer;

    /**
     * rpc服务器运行状态
     */
    private final AtomicBoolean running = new AtomicBoolean();

    private Thread asyncServiceRegistrationUnregistrationThread;

    /**
     * 当前rpc服务器所持有的所有的连接
     */
    private List<RpcConnection> connections = new LinkedList<>();

    protected RpcServer(RpcServerConfig config, ServiceDescriptorContainer serviceDescriptorContainer) {
        this.config = config;
        this.serviceDescriptorContainer = serviceDescriptorContainer;
        if (config.isEnableServiceRegistration()) {
            asyncServiceRegistrationUnregistrationQueue =
                    new LinkedBlockingQueue<>(config.getAsyncServiceRegistrationUnregistrationQueueSize());
        }
    }

    public void configure(RpcServerConfig serverConfig) {
        // do nothing
    }

    public int getPort() {
        return config.getPort();
    }

    public abstract InetSocketAddress getLocalAddress();

    //--------------------------------Lifecycle

    @Override
    public void start() {
        List<ServiceDescriptor> serviceDescriptors = serviceDescriptorContainer.getAll();
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Rpc server has already start");
        }
        // 1、初始化所有的过滤器
        if (config.isEnableServiceRegistration()) {
            // 2、将所有注册的服务暴露到服务注册中心（如果支持服务注册中心）
            // 3、启动异步线程，注册运行期间注册的服务
            asyncServiceRegistrationUnregistrationThread = new AsyncServiceRegistrationUnregistrationThread();
            asyncServiceRegistrationUnregistrationThread.start();
        }
        // 4、启动socket服务器
        internalStart(config);
    }

    protected abstract void internalStart(RpcServerConfig config);

    @Override
    public void shutdown() {
        if (!running.compareAndSet(true, false)) {
            throw new IllegalStateException("Rpc server has already shutdown");
        }
        internalShutdown();
        if (config.isEnableServiceRegistration()) {
            // 关闭和服务注册中心的连接
            if (asyncServiceRegistrationUnregistrationThread.isAlive()) {
                try {
                    asyncServiceRegistrationUnregistrationThread.join();
                }
                catch (InterruptedException e) {
                    LOG.warn("asyncServiceRegistrationUnregistrationThread has been interrupted", e);
                }
            }
            asyncServiceRegistrationUnregistrationThread = null;
            asyncServiceRegistrationUnregistrationQueue.clear();
        }
        serviceDescriptorContainer.clear();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    protected abstract void internalShutdown();

    //--------------------------------ServiceRegistrar

    @Override
    public <E> Optional<ServiceDescriptor> register(E rpcService) {
        if (Objects.isNull(rpcService)) {
            throw new NullPointerException("rpcService");
        }
        Optional<ServiceDescriptor> serviceDescriptor = serviceDescriptorContainer.register(rpcService);
        serviceDescriptor.ifPresent(this::registToServiceRegistrationIfNecessary);
        return serviceDescriptor;
    }

    private void registToServiceRegistrationIfNecessary(ServiceDescriptor serviceDescriptor) {
        if (config.isEnableServiceRegistration() && isRunning()) {
            multiRegisterToServiceRegistrationIfNecessary(List.of(serviceDescriptor));
        }
    }

    @Override
    public <E> Optional<ServiceDescriptor> register(Class<? super E> interfaceClass, E rpcService) {
        if (Objects.isNull(interfaceClass)) {
            return register(rpcService);
        }
        else if (Objects.isNull(rpcService)) {
            throw new NullPointerException("rpcService");
        }
        else {
            Optional<ServiceDescriptor> serviceDescriptor = serviceDescriptorContainer.register(interfaceClass, rpcService);
            serviceDescriptor.ifPresent(this::registToServiceRegistrationIfNecessary);
            return serviceDescriptor;
        }
    }

    @Override
    public <E> List<ServiceDescriptor> registerAll(Collection<?> rpcServices) {
        Objects.requireNonNull(rpcServices, "rpcServices");
        List<ServiceDescriptor> serviceDescriptors;
        if (rpcServices.isEmpty()) {
            serviceDescriptors = List.of();
            LOG.warn("Empty rpcServiecs and do nothing");
        }
        else {
            serviceDescriptors = serviceDescriptorContainer.<E>registerAll(rpcServices);
            multiRegisterToServiceRegistrationIfNecessary(serviceDescriptors);
        }
        return serviceDescriptors;
    }

    private void multiRegisterToServiceRegistrationIfNecessary(List<ServiceDescriptor> serviceDescriptors) {
        if (config.isEnableServiceRegistration() && isRunning() && !serviceDescriptors.isEmpty()) {
            Partition<ServiceDescriptor> sdPartition = new Partition<>(serviceDescriptors);
            while (sdPartition.hasNext()) {
                List<ServiceDescriptor> sub = sdPartition.next();
                if (!asyncServiceRegistrationUnregistrationQueue.offer(new RegistrationUnregistrationInfo(sub, true))) {
                    // 此时，注册队列已经满了，那么注册失败，移除之前注册的所有服务
                    unregisterAll(serviceDescriptors, false);
                    throw new IllegalStateException("asyncServiceRegistrationUnregistrationQueue has been fulled");
                }
//                try {
//                    asyncServiceRegistrationQueue.put(sub);
//                }
//                catch (InterruptedException e) {
//                    throw new IllegalStateException(e);
//                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Regist service: {} to service registration", serviceDescriptors);
            }
        }
    }

    @Override
    public <E> List<ServiceDescriptor> registerAll(Map<Class<?>, ?> rpcServices) {
        Objects.requireNonNull(rpcServices, "rpcServices");
        List<ServiceDescriptor> serviceDescriptors;
        if (rpcServices.isEmpty()) {
            serviceDescriptors = Collections.emptyList();
            LOG.warn("Empty rpcServiecs and do nothing");
        }
        else {
            serviceDescriptors = serviceDescriptorContainer.<E>registerAll(rpcServices);
            multiRegisterToServiceRegistrationIfNecessary(serviceDescriptors);
        }
        return serviceDescriptors;
    }

    @Override
    public boolean unregister(ServiceDescriptor serviceDescriptor) {
        Objects.requireNonNull(serviceDescriptor, "serviceDescriptor");
        unregisterFromServiceRegistrationIfNecessary(serviceDescriptor);
        return serviceDescriptorContainer.unregister(serviceDescriptor);
    }

    private void unregisterFromServiceRegistrationIfNecessary(ServiceDescriptor serviceDescriptor) {
        if (config.isEnableServiceRegistration()) {
            multiUnregisterFromServiceRegistrationIfNecessary(List.of(serviceDescriptor));
        }
    }

    private void multiUnregisterFromServiceRegistrationIfNecessary(List<ServiceDescriptor> serviceDescriptors) {
        if (config.isEnableServiceRegistration() && !serviceDescriptors.isEmpty()) {
            Partition<ServiceDescriptor> sdPartition = new Partition<>(serviceDescriptors);
            while (sdPartition.hasNext()) {
                List<ServiceDescriptor> sub = sdPartition.next();
                if (!asyncServiceRegistrationUnregistrationQueue.offer(new RegistrationUnregistrationInfo(sub, false))) {
                    throw new IllegalStateException("asyncServiceRegistrationUnregistrationQueue has been fulled");
                }
            }
        }
    }

    private boolean unregisterAll(List<ServiceDescriptor> serviceDescriptors, boolean unregisterFromServiceRegistration) {
        Objects.requireNonNull(serviceDescriptors, "serviceDescriptors");
        if (unregisterFromServiceRegistration) {
            multiUnregisterFromServiceRegistrationIfNecessary(serviceDescriptors);
        }
        return !serviceDescriptors.isEmpty() && serviceDescriptorContainer.unregisterAll(serviceDescriptors);
    }

    @Override
    public boolean unregisterAll(List<ServiceDescriptor> serviceDescriptors) {
        return unregisterAll(serviceDescriptors, true);
    }

    private class AsyncServiceRegistrationUnregistrationThread extends QrpcThread {

        private AsyncServiceRegistrationUnregistrationThread() {
            super("ASRU");
        }

        @Override
        public void run() {
            while (isRunning()) {
                RegistrationUnregistrationInfo info;
                try {
                    info = asyncServiceRegistrationUnregistrationQueue.take();
                }
                catch (InterruptedException e) {
                    // ignore and log message
                    LOG.warn("Blocking queue[asyncServiceRegistrationUnregistrationQueue] " +
                            "has been interrupted while waiting for an serviceDescriptor list", e);
                    continue;
                }
                List<ServiceDescriptor> serviceDescriptors = info.serviceDescriptors;
                if (info.regist) {
                    // 注册服务

                }
                else {
                    // 注销服务
                }
            }
        }
    }

    private static class RegistrationUnregistrationInfo {

        private boolean regist;

        private List<ServiceDescriptor> serviceDescriptors;

        private RegistrationUnregistrationInfo(List<ServiceDescriptor> serviceDescriptors, boolean regist) {
            this.serviceDescriptors = serviceDescriptors;
            this.regist = regist;
        }
    }
}
