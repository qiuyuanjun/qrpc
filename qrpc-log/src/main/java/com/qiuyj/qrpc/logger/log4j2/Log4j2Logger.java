package com.qiuyj.qrpc.logger.log4j2;

import com.qiuyj.qrpc.logger.InternalLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * log4j2日志框架实现
 * @author qiuyj
 * @since 2020-02-25
 */
class Log4j2Logger implements InternalLogger {

    private final Logger log4j2Impl;

    Log4j2Logger(String name) {
        log4j2Impl = LogManager.getLogger(name);
    }

    @Override
    public boolean isErrorEnabled() {
        return log4j2Impl.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        log4j2Impl.error(msg);
    }

    @Override
    public void error(String msg, Throwable e) {
        log4j2Impl.error(msg, e);
    }

    @Override
    public void error(String msg, Object... args) {
        log4j2Impl.error(msg, args);
    }

    @Override
    public boolean isWarnEnabled() {
        return log4j2Impl.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        log4j2Impl.warn(msg);
    }

    @Override
    public void warn(String msg, Throwable e) {
        log4j2Impl.warn(msg, e);
    }

    @Override
    public void warn(String msg, Object... args) {
        log4j2Impl.warn(msg, args);
    }

    @Override
    public boolean isInfoEnabled() {
        return log4j2Impl.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        log4j2Impl.info(msg);
    }

    @Override
    public void info(String msg, Throwable e) {
        log4j2Impl.info(msg, e);
    }

    @Override
    public void info(String msg, Object... args) {
        log4j2Impl.info(msg, args);
    }

    @Override
    public boolean isDebugEnabled() {
        return log4j2Impl.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        log4j2Impl.debug(msg);
    }

    @Override
    public void debug(String msg, Throwable e) {
        log4j2Impl.debug(msg, e);
    }

    @Override
    public void debug(String msg, Object... args) {
        log4j2Impl.debug(msg, args);
    }
}
