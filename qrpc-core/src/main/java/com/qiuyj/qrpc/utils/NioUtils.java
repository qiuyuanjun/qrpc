package com.qiuyj.qrpc.utils;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2020-03-06
 */
public abstract class NioUtils {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(NioUtils.class);

    private NioUtils() {
        // for private
    }

    public static void closeSocketChannelQuietly(SocketChannel sc) {
        if (Objects.nonNull(sc)) {
            if (sc.isConnected()) {
                try {
                    sc.shutdownInput();
                }
                catch (IOException e) {
                    LOG.warn("Ignore the exception that failed to close the input of channel", e);
                }

                try {
                    sc.shutdownOutput();
                }
                catch (IOException e) {
                    LOG.warn("Ignore the exception that failed to close the output of channel", e);
                }
            }

            try {
                sc.close();
            }
            catch (IOException e) {
                LOG.warn("Ignore the exception that failed to close the socket channel", e);
            }
        }
    }

    public static void closeServerSocketChannelQuietly(ServerSocketChannel ss) {
        if (Objects.nonNull(ss)) {
            try {
                ss.close();
            }
            catch (IOException e) {
                LOG.warn("Ignore the exception that failed to close the server socket channel", e);
            }
        }
    }

    public static void closeSelectorQuietly(Selector sel) {
        if (Objects.nonNull(sel)) {
            try {
                sel.close();
            }
            catch (IOException e) {
                LOG.warn("Ignore the exception that failed to close the selector", e);
            }
        }
    }

    public static void cancelSelectionKey(SelectionKey sk) {
        if (Objects.nonNull(sk)) {
            sk.cancel();
        }
    }
}
