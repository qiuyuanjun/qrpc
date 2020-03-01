package com.qiuyj.qrpc.logger.slf4j;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

/**
 * @author qiuyj
 * @since 2020-02-26
 */
public class Slf4jLoggerFactory extends InternalLoggerFactory {

    public static final Slf4jLoggerFactory INSTANCE = new Slf4jLoggerFactory();

    private Slf4jLoggerFactory() {
        // for private
    }

    @Override
    protected InternalLogger newInstance(String name) {
        return new Slf4jLogger(name);
    }
}
