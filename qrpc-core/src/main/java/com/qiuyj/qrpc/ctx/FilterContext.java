package com.qiuyj.qrpc.ctx;

import com.qiuyj.qrpc.filter.Filter;
import com.qiuyj.qrpc.invoke.MethodInvocation;
import com.qiuyj.qrpc.invoke.MethodInvoker;
import com.qiuyj.qrpc.message.payload.RpcResult;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 过滤器上下文，每一次执行所有的过滤器，都需要新建一个过滤器上下文
 * @author qiuyj
 * @since 2020-03-22
 */
public class FilterContext {

    /**
     * 所有要执行的过滤器
     */
    private List<Filter> filters;

    /**
     * 当前执行的过滤器的下标
     */
    private int cursor = -1;

    private MethodInvoker invoker;

    private MethodInvocation invocation;

    /**
     * 上下文
     */
    private Map<String, Object> context;

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    /**
     * 执行结果
     */
    private RpcResult result;

    public FilterContext(List<Filter> filters, MethodInvoker invoker, MethodInvocation invocation) {
        this.filters = filters;
        this.invoker = invoker;
        this.invocation = invocation;
    }

    public FilterContext(Map<String, Object> map, List<Filter> filters, MethodInvoker invoker, MethodInvocation invocation) {
        this.context = new HashMap<>(map);
        this.filters = filters;
        this.invoker = invoker;
        this.invocation = invocation;
    }

    /**
     * 执行下一个过滤器
     */
    public void fireNextFilter() {
        if (cursor == -1 && Objects.isNull(context)) {
            // 第一次进入过滤器链，初始化上下文
            context = new HashMap<>();
        }
        if (++cursor < filters.size()) {
            // 还有过滤器没有执行，那么继续执行所有的过滤器
            filters.get(cursor).filter(this);
        }
        else {
            // 执行具体的方法
            result = invoker.invoke(invocation);
            // 方法执行完毕，那么清空上下文信息
            clearContext();
        }
    }

    private void clearContext() {
        context.clear();
        RpcContext.getContextIfAvailable().ifPresent(RpcContext::clearContext);
    }

    public Class<?> getInterface() {
        return invocation.getInterfaceClass();
    }

    public Class<?>[] getMethodArgTypes() {
        return invocation.getMethodArgTypes();
    }

    public Object[] getMethodArgs() {
        return invocation.getMethodArgs();
    }

    public void addContextValue(String key, Object value) {
        if (Objects.isNull(context)) {
            context = new HashMap<>();
        }
        Optional.ofNullable(value)
                .ifPresentOrElse(v -> context.put(key, v), () -> context.remove(key));
        // 如果此时可以获取RpcContext，那么同时也需要将值添加到RpcContext里面
        RpcContext.getContextIfAvailable()
                .ifPresent(c -> c.add(key, value));
    }

    public Map<String, Object> getContext() {
        return Objects.nonNull(context) ? Collections.unmodifiableMap(context) : null;
    }

    public void setInetAddress(InetSocketAddress remoteAddress, InetSocketAddress localAddress) {
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public RpcResult getResult() {
        return result;
    }
}
