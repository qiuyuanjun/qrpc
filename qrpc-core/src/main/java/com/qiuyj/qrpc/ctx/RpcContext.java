package com.qiuyj.qrpc.ctx;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * rpc上下文
 * @author qiuyj
 * @since 2020-03-22
 */
public class RpcContext {

    private static final ThreadLocal<RpcContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public static final String KEY_SERVER_SIDE = "serverSide";

    /**
     * 得到当前线程的rpc上下文对象
     */
    public static Optional<RpcContext> getContextIfAvailable() {
        return Optional.ofNullable(CONTEXT_HOLDER.get());
    }

    public static RpcContext getContext() {
        RpcContext context = CONTEXT_HOLDER.get();
        if (Objects.isNull(context)) {
            throw new IllegalStateException("The rpc context not set yet");
        }
        return context;
    }

    static RpcContext initContext() {
        CONTEXT_HOLDER.set(new RpcContext());
        return CONTEXT_HOLDER.get();
    }

    static void removeContext() {
        CONTEXT_HOLDER.remove();
    }

    private final Map<String, Object> attachment = new HashMap<>();

    private InetSocketAddress remoteAddress;

    private InetSocketAddress localAddress;

    private RpcContext() {
        // for private
    }

    public void add(String key, Object value) {
        Optional.ofNullable(value)
                .ifPresentOrElse(v -> attachment.put(key, v), () -> attachment.remove(key));
    }

    public void addAll(Map<String, Object> map) {
        map.forEach(this::add);
    }

    public Object get(String key) {
        return attachment.get(key);
    }

    public boolean isServerSide() {
        return Boolean.TRUE.equals(get(KEY_SERVER_SIDE));
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public Map<String, Object> getAttachment() {
        return attachment;
    }

    /**
     * 设置{@code remoteAddress}和{@code localAddress}
     * @param remoteAddress {@code remoteAddress}
     * @param localAddress {@code localAddress}
     */
    void setInetAddress(InetSocketAddress remoteAddress, InetSocketAddress localAddress) {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
    }

    /**
     * 设置{@link #KEY_SERVER_SIDE}为{@code true}
     */
    void serverSide() {
        attachment.put(KEY_SERVER_SIDE, Boolean.TRUE);
    }

    /**
     * 清空上下文
     */
    void clearContext() {
        attachment.clear();
    }
}
