package com.qiuyj.qrpc.test;

import com.qiuyj.qrpc.server.RpcServer;
import com.qiuyj.qrpc.server.RpcServerFactory;

/**
 * @author qiuyj
 * @since 2020-03-14
 */
public class RpcServerTest {

    public static void main(String[] args) {
        RpcServer server = RpcServerFactory.createDefault();
        server.start();
        System.out.println(server.isRunning());
    }
}
