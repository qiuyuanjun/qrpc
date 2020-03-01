/**
 * @author qiuyj
 * @since 2020-02-25
 */
module qrpc.server {
    requires transitive qrpc.core;
    requires static io.netty.all;

    exports com.qiuyj.qrpc.server;
    exports com.qiuyj.qrpc.service to qrpc.spring;
}