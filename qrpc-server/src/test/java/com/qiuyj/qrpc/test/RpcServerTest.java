package com.qiuyj.qrpc.test;

import com.qiuyj.qrpc.invoke.MethodHandleMethodInvocation;
import com.qiuyj.qrpc.server.RpcServer;
import com.qiuyj.qrpc.server.RpcServerFactory;
import com.qiuyj.qrpc.service.ServiceRegistrar;

/**
 * @author qiuyj
 * @since 2020-03-14
 */
public class RpcServerTest {

    public static void main(String[] args) throws Throwable {
        RpcServer server = RpcServerFactory.createDefault();
        server.start();

        MethodHandleMethodInvocation mi = new MethodHandleMethodInvocation(server.getClass().getMethod("register", Class.class, Object.class),
                ServiceRegistrar.class,
                server,
                ServiceRegistrar.class,
                server);
        mi.proceed();

        System.out.println(server.getPort());
    }
}
