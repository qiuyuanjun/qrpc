package com.qiuyj.qrpc.logger.log4j;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

/**
 * @author qiuyj
 * @since 2020-02-29
 */
public class Log4jLoggerFactory extends InternalLoggerFactory {

    public static final Log4jLoggerFactory INSTANCE = new Log4jLoggerFactory();

    private Log4jLoggerFactory() {
        // for private
    }

    @Override
    protected InternalLogger newInstance(String name) {
        return new Log4jLogger(name);
    }
}
