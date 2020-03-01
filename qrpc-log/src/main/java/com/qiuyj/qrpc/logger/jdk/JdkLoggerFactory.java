package com.qiuyj.qrpc.logger.jdk;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

/**
 * @author qiuyj
 * @since 2020-02-25
 */
public class JdkLoggerFactory extends InternalLoggerFactory {

    public static final JdkLoggerFactory INSTANCE = new JdkLoggerFactory();

    private JdkLoggerFactory() {
        // for private
    }

    @Override
    protected InternalLogger newInstance(String name) {
        return new JdkLogger(name);
    }
}
