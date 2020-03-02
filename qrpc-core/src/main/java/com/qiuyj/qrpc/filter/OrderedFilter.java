package com.qiuyj.qrpc.filter;

/**
 * @author qiuyj
 * @since 2020-03-02
 */
public interface OrderedFilter extends Filter {

    int MAX_ORDER = Integer.MIN_VALUE;

    int MIN_ORDER = Integer.MAX_VALUE;

    /**
     * 当前order的顺序，按自然顺序排序，越小越在前面执行
     */
    int getOrder();
}
