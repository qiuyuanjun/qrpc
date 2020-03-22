package com.qiuyj.qrpc.client;

import com.qiuyj.qrpc.filter.Filter;

/**
 * 客户端专用过滤器
 * @author qiuyj
 * @since 2020-03-21
 */
public abstract class ClientFilter implements Filter {

    @Override
    public FilterType getType() {
        return FilterType.FT_CLIENT;
    }
}
