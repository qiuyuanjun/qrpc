package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.filter.Filter;

/**
 * 服务器端专用过滤器
 * @author qiuyj
 * @since 2020-03-21
 */
public abstract class ServerFilter implements Filter {

    @Override
    public FilterType getType() {
        return FilterType.FT_SERVER;
    }
}
