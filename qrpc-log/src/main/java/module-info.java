/**
 * @author qiuyj
 * @since 2020-02-26
 */
module qrpc.log {
    requires static java.logging;
    requires static org.slf4j;
    requires static org.apache.logging.log4j;
    requires static log4j;

    exports com.qiuyj.qrpc.logger;
}