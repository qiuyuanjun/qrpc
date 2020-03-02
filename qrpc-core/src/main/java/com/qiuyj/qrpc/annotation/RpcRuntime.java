package com.qiuyj.qrpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 配置运行时的rpc服务各种参数
 * @author qiuyj
 * @since 2020-03-02
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcRuntime {

    /**
     * 是否异步执行服务，默认{@code false}，客户端选项
     */
    boolean async() default false;

    /**
     * 超时时间，默认没有超时时间
     */
    int timeout() default 0;

    /**
     * 时间单位，和timeout属性配合使用，默认为毫秒
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 重试次数，客户端选项
     */
    int retry() default 0;

    /**
     * 是否从服务注册中心检测连接的服务已经注册，客户端选项
     */
    boolean check() default true;

    /**
     * 当前服务版本
     */
    String version() default "";

    /**
     * 服务直连url（不通过注册中心获取对应的服务的ip），客户端选项
     */
    String serviceUrl() default "";
}
