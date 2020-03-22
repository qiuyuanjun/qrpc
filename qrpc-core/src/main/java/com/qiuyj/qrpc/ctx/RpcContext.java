package com.qiuyj.qrpc.ctx;

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

    private final Map<String, Object> context = new HashMap<>();

    private RpcContext() {
        // for private
    }

    public void add(String key, Object value) {
        Optional.ofNullable(value)
                .ifPresentOrElse(v -> context.put(key, v), () -> context.remove(key));
    }

    public void addAll(Map<String, Object> map) {
        map.forEach(this::add);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public boolean isServerSide() {
        return Boolean.TRUE.equals(get(KEY_SERVER_SIDE));
    }

    /**
     * 设置{@link #KEY_SERVER_SIDE}为{@code true}
     */
    void serverSide() {
        context.put(KEY_SERVER_SIDE, Boolean.TRUE);
    }
}
