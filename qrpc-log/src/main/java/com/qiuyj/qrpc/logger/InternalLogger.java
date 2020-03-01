package com.qiuyj.qrpc.logger;

/**
 * 内部统一日志处理顶级接口
 * @author qiuyj
 * @since 2020-02-25
 */
public interface InternalLogger {

    boolean isErrorEnabled();

    void error(String msg);

    void error(String msg, Throwable e);

    void error(String msg, Object... args);

    boolean isWarnEnabled();

    void warn(String msg);

    void warn(String msg, Throwable e);

    void warn(String msg, Object... args);

    boolean isInfoEnabled();

    void info(String msg);

    void info(String msg, Throwable e);

    void info(String msg, Object... args);

    boolean isDebugEnabled();

    void debug(String msg);

    void debug(String msg, Throwable e);

    void debug(String msg, Object... args);
}
