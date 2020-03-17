/**
 * @author qiuyj
 * @since 2020-02-25
 */
module qrpc.server {
    requires transitive qrpc.transport;
    requires static io.netty.all;

    exports com.qiuyj.qrpc.server;
    exports com.qiuyj.qrpc.server.netty to qrpc.spring;
    exports com.qiuyj.qrpc.server.nio to qrpc.spring;
    exports com.qiuyj.qrpc.service to qrpc.spring;
}