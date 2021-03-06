package com.qiuyj.qrpc.server;

/**
 * 生命周期接口
 * @author qiuyj
 * @since 2020-02-29
 */
public interface Lifecycle {

    void start();

    void shutdown();

    boolean isRunning();
}
