package com.qiuyj.qrpc.logger.slf4j;

import com.qiuyj.qrpc.logger.InternalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * slf4j适配层实现
 * @author qiuyj
 * @since 2020-02-26
 */

class Slf4jLogger implements InternalLogger {

    private final Logger slf4jImpl;

    Slf4jLogger(String name) {
        slf4jImpl = LoggerFactory.getLogger(name);
    }

    @Override
    public boolean isErrorEnabled() {
        return slf4jImpl.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        slf4jImpl.error(msg);
    }

    @Override
    public void error(String msg, Throwable e) {
        slf4jImpl.error(msg, e);
    }

    @Override
    public void error(String msg, Object... args) {
        slf4jImpl.error(msg, args);
    }

    @Override
    public boolean isWarnEnabled() {
        return slf4jImpl.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        slf4jImpl.warn(msg);
    }

    @Override
    public void warn(String msg, Throwable e) {
        slf4jImpl.warn(msg, e);
    }

    @Override
    public void warn(String msg, Object... args) {
        slf4jImpl.warn(msg, args);
    }

    @Override
    public boolean isInfoEnabled() {
        return slf4jImpl.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        slf4jImpl.info(msg);
    }

    @Override
    public void info(String msg, Throwable e) {
        slf4jImpl.info(msg, e);
    }

    @Override
    public void info(String msg, Object... args) {
        slf4jImpl.info(msg, args);
    }

    @Override
    public boolean isDebugEnabled() {
        return slf4jImpl.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        slf4jImpl.debug(msg);
    }

    @Override
    public void debug(String msg, Throwable e) {
        slf4jImpl.debug(msg, e);
    }

    @Override
    public void debug(String msg, Object... args) {
        slf4jImpl.debug(msg, args);
    }
}
