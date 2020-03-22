package com.qiuyj.qrpc.filter;

/**
 * 服务执行过滤器，包括客户端和服务器端执行
 * @author qiuyj
 * @since 2020-03-02
 */
public interface Filter {

    /**
     * filter类型
     */
    enum FilterType {

        /**
         * 服务器端过滤器
         */
        FT_SERVER,

        /**
         * 客户端过滤器
         */
        FT_CLIENT,

        /**
         * 服务器端和客户端通用过滤器
         */
        FT_ALL
    }

    /**
     * 得到当前过滤器的类型，默认服务器端和客户端通用过滤器
     */
    default FilterType getType() {
        return FilterType.FT_ALL;
    }

    /**
     * 执行过滤器链
     */
    void filter(FilterContext context);
}
