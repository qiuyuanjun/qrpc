package com.qiuyj.qrpc;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

/**
 * @author qiuyj
 * @since 2020-03-03
 */
public class QrpcThread extends Thread {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(QrpcThread.class);

    public QrpcThread(String name) {
        this(null, name);
    }

    public QrpcThread(Runnable target, String name) {
        super(target, "QrpcThread-" + name);
        setUncaughtExceptionHandler(this::handleException);
    }

    protected void handleException(Thread t, Throwable e) {
        LOG.error("Exception occurred from thread " + t.getName(), e);
    }
}
