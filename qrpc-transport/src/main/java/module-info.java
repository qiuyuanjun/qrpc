/**
 * @author qiuyj
 * @since 2020-03-15
 */
module qrpc.transport {
    requires transitive qrpc.core;

    exports com.qiuyj.qrpc.cnxn to qrpc.server;
    exports com.qiuyj.qrpc.message;
    exports com.qiuyj.qrpc.message.converter;
}