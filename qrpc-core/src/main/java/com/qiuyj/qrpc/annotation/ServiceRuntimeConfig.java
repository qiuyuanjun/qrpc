package com.qiuyj.qrpc.annotation;

/**
 * {@link RpcRuntime}配置实例接口
 * @author qiuyj
 * @since 2020-03-03
 */
public interface ServiceRuntimeConfig {

    boolean isAsync();

    int getTimeout();

    int getRetry();

    boolean isCheck();

    String getVersion();

    String getServiceUrl();
}
