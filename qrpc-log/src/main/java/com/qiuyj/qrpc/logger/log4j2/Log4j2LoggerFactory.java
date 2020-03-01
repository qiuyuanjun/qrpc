package com.qiuyj.qrpc.logger.log4j2;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

/**
 * @author qiuyj
 * @since 2020-02-26
 */
public class Log4j2LoggerFactory extends InternalLoggerFactory {

    public static final Log4j2LoggerFactory INSTANCE = new Log4j2LoggerFactory();

    private Log4j2LoggerFactory() {
        // for private
    }

    @Override
    protected InternalLogger newInstance(String name) {
        return new Log4j2Logger(name);
    }
}
