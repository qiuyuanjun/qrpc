/**
 * @author qiuyj
 * @since 2020-02-29
 */
module qrpc.core {
    requires transitive qrpc.log;

    exports com.qiuyj.qrpc;
    exports com.qiuyj.qrpc.utils to qrpc.server;
}