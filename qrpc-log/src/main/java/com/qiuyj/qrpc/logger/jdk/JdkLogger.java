package com.qiuyj.qrpc.logger.jdk;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.log4j.FormattingTuple;
import com.qiuyj.qrpc.logger.log4j.MessageFormatter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * jdk自带的jul日志框架实现
 * @author qiuyj
 * @since 2020-02-25
 */
class JdkLogger implements InternalLogger {

    private final Logger jdkImpl;

    JdkLogger(String name) {
        jdkImpl = Logger.getLogger(name);
    }

    @Override
    public boolean isErrorEnabled() {
        return jdkImpl.isLoggable(Level.SEVERE);
    }

    @Override
    public void error(String msg) {
        jdkImpl.log(Level.SEVERE, msg);
    }

    @Override
    public void error(String msg, Throwable e) {
        jdkImpl.log(Level.SEVERE, msg, e);
    }

    @Override
    public void error(String msg, Object... args) {
        if (isErrorEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
            error(ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return jdkImpl.isLoggable(Level.WARNING);
    }

    @Override
    public void warn(String msg) {
        jdkImpl.log(Level.WARNING, msg);
    }

    @Override
    public void warn(String msg, Throwable e) {
        jdkImpl.log(Level.WARNING, msg, e);
    }

    @Override
    public void warn(String msg, Object... args) {
        if (isWarnEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
            warn(ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return jdkImpl.isLoggable(Level.INFO);
    }

    @Override
    public void info(String msg) {
        jdkImpl.log(Level.INFO, msg);
    }

    @Override
    public void info(String msg, Throwable e) {
        jdkImpl.log(Level.INFO, msg, e);
    }

    @Override
    public void info(String msg, Object... args) {
        if (isInfoEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
            info(ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return jdkImpl.isLoggable(Level.FINE);
    }

    @Override
    public void debug(String msg) {
        jdkImpl.log(Level.FINE, msg);
    }

    @Override
    public void debug(String msg, Throwable e) {
        jdkImpl.log(Level.FINE, msg, e);
    }

    @Override
    public void debug(String msg, Object... args) {
        if (isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
            debug(ft.getMessage(), ft.getThrowable());
        }
    }
}
