package com.qiuyj.qrpc.ctx;

import com.qiuyj.qrpc.filter.OrderedFilter;

import java.util.Optional;

/**
 * @author qiuyj
 * @since 2020-03-22
 */
public class ContextFilter implements OrderedFilter {

    @Override
    public void filter(FilterContext context) {
        RpcContext rpcCtx = RpcContext.initContext();
        Optional.ofNullable(context.getContext())
                .ifPresent(rpcCtx::addAll);
        rpcCtx.setInetAddress(context.getRemoteAddress(), context.getLocalAddress());
        try {
            if (isServerSide()) {
                rpcCtx.serverSide();
            }
            context.fireNextFilter();
        }
        finally {
            RpcContext.removeContext();
        }
    }

    private boolean isServerSide() {
        RpcContext rpcCtx = RpcContext.getContext();

        return true;
    }

    @Override
    public int getOrder() {
        return OrderedFilter.MAX_ORDER;
    }
}
