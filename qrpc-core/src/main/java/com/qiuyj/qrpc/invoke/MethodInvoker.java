package com.qiuyj.qrpc.invoke;

/**
 * 方法执行器，用于执行具体的方法
 * @author qiuyj
 * @since 2020-03-22
 */
@FunctionalInterface
public interface MethodInvoker {

    /**
     * 执行给定的方法
     * @param invocation 方法定义
     * @return 方法返回值
     */
    Object invoke(MethodInvocation invocation);
}
