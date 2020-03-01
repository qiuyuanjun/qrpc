package com.qiuyj.qrpc.logger;

import com.qiuyj.qrpc.logger.jdk.JdkLoggerFactory;
import com.qiuyj.qrpc.logger.log4j.Log4jLoggerFactory;
import com.qiuyj.qrpc.logger.log4j2.Log4j2LoggerFactory;
import com.qiuyj.qrpc.logger.slf4j.Slf4jLoggerFactory;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2020-02-25
 */
public abstract class InternalLoggerFactory {

    private static volatile InternalLoggerFactory defaultFactory;

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void setDefaultLoggerFactory(InternalLoggerFactory defaultFactory) {
        InternalLoggerFactory.defaultFactory = defaultFactory;
    }

    public static InternalLoggerFactory getDefaultFactory() {
        if (Objects.isNull(defaultFactory)) {
            synchronized (InternalLoggerFactory.class) {
                if (Objects.isNull(defaultFactory)) {
                    if (isClassPresent("org.slf4j.Logger")) {
                        defaultFactory = Slf4jLoggerFactory.INSTANCE;
                    }
                    else if (isClassPresent("org.apache.logging.log4j.Logger")) {
                        defaultFactory = Log4j2LoggerFactory.INSTANCE;
                    }
                    else if (isClassPresent("org.apache.log4j.Logger")) {
                        defaultFactory = Log4jLoggerFactory.INSTANCE;
                    }
                    else {
                        defaultFactory = JdkLoggerFactory.INSTANCE;
                    }
                }
            }
        }
        return defaultFactory;
    }

    public static InternalLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static InternalLogger getLogger(String name) {
        return getDefaultFactory().newInstance(name);
    }

    protected abstract InternalLogger newInstance(String name);
}
