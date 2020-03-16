/**
 * @author qiuyj
 * @since 2020-03-15
 */
module qrpc.message {
    requires transitive qrpc.core;

    exports com.qiuyj.qrpc.message;
    exports com.qiuyj.qrpc.message.converter;
    exports com.qiuyj.qrpc.message.payload;
}