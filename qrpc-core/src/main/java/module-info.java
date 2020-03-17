/**
 * @author qiuyj
 * @since 2020-02-29
 */
module qrpc.core {
    requires transitive qrpc.log;
    requires transitive jdk.unsupported;

    exports com.qiuyj.qrpc;
    exports com.qiuyj.qrpc.annotation;
    exports com.qiuyj.qrpc.filter;
    exports com.qiuyj.qrpc.utils to qrpc.server, qrpc.transport;
}