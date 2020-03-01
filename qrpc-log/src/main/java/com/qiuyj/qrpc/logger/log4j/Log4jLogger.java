package com.qiuyj.qrpc.logger.log4j;

import com.qiuyj.qrpc.logger.InternalLogger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * log4j日志框架实现
 * @author qiuyj
 * @since 2020-02-29
 */
class Log4jLogger implements InternalLogger {

    private static final String FQCN = "com.qiuyj.qrpc.logger.log4j.Log4jLogger";

    private final Logger log4jImpl;

    Log4jLogger(String name) {
        log4jImpl = Logger.getLogger(name);
    }

    @Override
    public boolean isErrorEnabled() {
        return log4jImpl.isEnabledFor(Level.ERROR);
    }

    @Override
    public void error(String msg) {
        log4jImpl.error(msg);
    }

    @Override
    public void error(String msg, Throwable e) {
        log4jImpl.error(msg, e);
    }

    @Override
    public void error(String msg, Object... args) {
        if (isErrorEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
            log4jImpl.log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return log4jImpl.isEnabledFor(Level.WARN);
    }

    @Override
    public void warn(String msg) {
        log4jImpl.warn(msg);
    }

    @Override
    public void warn(String msg, Throwable e) {
        log4jImpl.warn(msg, e);
    }

    @Override
    public void warn(String msg, Object... args) {
        if (isWarnEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
            log4jImpl.log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return log4jImpl.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        log4jImpl.info(msg);
    }

    @Override
    public void info(String msg, Throwable e) {
        log4jImpl.info(msg, e);
    }

    @Override
    public void info(String msg, Object... args) {
        if (isInfoEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
            log4jImpl.log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return log4jImpl.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        log4jImpl.debug(msg);
    }

    @Override
    public void debug(String msg, Throwable e) {
        log4jImpl.debug(msg, e);
    }

    @Override
    public void debug(String msg, Object... args) {
        if (isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
            log4jImpl.log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
        }
    }
}
