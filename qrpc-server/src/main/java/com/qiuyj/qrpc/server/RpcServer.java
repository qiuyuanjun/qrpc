package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.QrpcThread;
import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.service.ServiceDescriptor;
import com.qiuyj.qrpc.service.ServiceDescriptorContainer;
import com.qiuyj.qrpc.service.ServiceRegistrar;
import com.qiuyj.qrpc.utils.Partition;

import java.util.Collection;
import java.util.Collections;
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

    private BlockingQueue<List<ServiceDescriptor>> asyncServiceRegistrationQueue;

    private RpcServerConfig config;

    private ServiceDescriptorContainer serviceDescriptorContainer;

    /**
     * rpc服务器运行状态
     */
    private final AtomicBoolean running = new AtomicBoolean();

    private Thread asyncServiceRegistrationThread;

    protected RpcServer(RpcServerConfig config, ServiceDescriptorContainer serviceDescriptorContainer) {
        this.config = config;
        this.serviceDescriptorContainer = serviceDescriptorContainer;
        asyncServiceRegistrationQueue = new LinkedBlockingQueue<>(config.getAsyncServiceRegistrationQueueSize());
    }

    public void configure() {

    }

    //--------------------------------Lifecycle

    @Override
    public void start() {
        List<ServiceDescriptor> serviceDescriptors = serviceDescriptorContainer.getAll();
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Rpc server has already start");
        }
        // 1、初始化所有的过滤器
        // 2、将所有注册的服务暴露到服务注册中心（如果支持服务注册中心）
        // 3、启动异步线程，注册运行期间注册的服务
        asyncServiceRegistrationThread = new AsyncServiceRegistrationThread();
        asyncServiceRegistrationThread.start();
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
        // 关闭和服务注册中心的连接
        if (asyncServiceRegistrationThread.isAlive()) {
            try {
                asyncServiceRegistrationThread.join();
            }
            catch (InterruptedException e) {
                LOG.warn("asyncServiceRegistrationThread has been interrupted", e);
            }
        }
        asyncServiceRegistrationThread = null;
        asyncServiceRegistrationQueue.clear();
        serviceDescriptorContainer.clear();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    protected abstract void internalShutdown();

    //--------------------------------ServiceRegistrar

    @Override
    public <E> Optional<ServiceDescriptor> regist(E rpcService) {
        if (Objects.isNull(rpcService)) {
            throw new NullPointerException("rpcService");
        }
        Optional<ServiceDescriptor> serviceDescriptor = serviceDescriptorContainer.regist(rpcService);
        serviceDescriptor.ifPresent(this::registToServiceRegistrationIfNecessary);
        return serviceDescriptor;
    }

    private void registToServiceRegistrationIfNecessary(ServiceDescriptor serviceDescriptor) {
        if (isRunning()) {
            multiRegistToServiceRegistrationIfNecessary(List.of(serviceDescriptor));
        }
    }

    @Override
    public <E> Optional<ServiceDescriptor> regist(Class<? super E> interfaceClass, E rpcService) {
        if (Objects.isNull(interfaceClass)) {
            return regist(rpcService);
        }
        else if (Objects.isNull(rpcService)) {
            throw new NullPointerException("rpcService");
        }
        else {
            Optional<ServiceDescriptor> serviceDescriptor = serviceDescriptorContainer.regist(interfaceClass, rpcService);
            serviceDescriptor.ifPresent(this::registToServiceRegistrationIfNecessary);
            return serviceDescriptor;
        }
    }

    @Override
    public <E> List<ServiceDescriptor> registAll(Collection<?> rpcServices) {
        Objects.requireNonNull(rpcServices, "rpcServices");
        List<ServiceDescriptor> serviceDescriptors;
        if (rpcServices.isEmpty()) {
            serviceDescriptors = List.of();
            LOG.warn("Empty rpcServiecs and do nothing");
        }
        else {
            serviceDescriptors = serviceDescriptorContainer.<E>registAll(rpcServices);
            multiRegistToServiceRegistrationIfNecessary(serviceDescriptors);
        }
        return serviceDescriptors;
    }

    private void multiRegistToServiceRegistrationIfNecessary(List<ServiceDescriptor> serviceDescriptors) {
        if (isRunning() && !serviceDescriptors.isEmpty()) {
            Partition<ServiceDescriptor> serviceProxyPartition = new Partition<>(serviceDescriptors);
            while (serviceProxyPartition.hasNext()) {
                List<ServiceDescriptor> sub = serviceProxyPartition.next();
                if (!asyncServiceRegistrationQueue.offer(sub)) {
                    // 此时，注册队列已经满了，那么注册失败，移除之前注册的所有服务
                    unregistAll(serviceDescriptors);
                    throw new IllegalStateException("asyncServiceRegistrationQueue has been fulled");
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
    public <E> List<ServiceDescriptor> registAll(Map<Class<?>, ?> rpcServices) {
        Objects.requireNonNull(rpcServices, "rpcServices");
        List<ServiceDescriptor> serviceDescriptors;
        if (rpcServices.isEmpty()) {
            serviceDescriptors = Collections.emptyList();
            LOG.warn("Empty rpcServiecs and do nothing");
        }
        else {
            serviceDescriptors = serviceDescriptorContainer.<E>registAll(rpcServices);
            multiRegistToServiceRegistrationIfNecessary(serviceDescriptors);
        }
        return serviceDescriptors;
    }

    @Override
    public boolean unregist(ServiceDescriptor serviceDescriptor) {
        Objects.requireNonNull(serviceDescriptor, "serviceDescriptor");
        return serviceDescriptorContainer.unregist(serviceDescriptor);
    }

    @Override
    public boolean unregistAll(List<ServiceDescriptor> serviceDescriptors) {
        Objects.requireNonNull(serviceDescriptors, "serviceDescriptors");
        return !serviceDescriptors.isEmpty() && serviceDescriptorContainer.unregistAll(serviceDescriptors);
    }

    private class AsyncServiceRegistrationThread extends QrpcThread {

        private AsyncServiceRegistrationThread() {
            super("asyncServiceRegistration");
        }

        @Override
        public void run() {
            while (isRunning()) {
                List<ServiceDescriptor> serviceDescriptors;
                try {
                    serviceDescriptors = asyncServiceRegistrationQueue.take();
                }
                catch (InterruptedException e) {
                    // ignore and log message
                    LOG.warn("Blocking queue[asyncServiceRegistrationQueue] " +
                            "has been interrupted while waiting for an serviceDescriptor list", e);
                    continue;
                }
                if (serviceDescriptors.size() == 1) {
                    // 执行单个服务实例注册
                }
                else {
                    // 执行多实例注册
                }
            }
        }
    }
}
