package com.qiuyj.qrpc.cnxn;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;

import java.io.IOException;

/**
 * @author qiuyj
 * @since 2020-03-15
 */
public abstract class AbstractRpcConnection implements RpcConnection {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(AbstractRpcConnection.class);

    @Override
    public void sendMessage(Object message) throws IOException {
        if (!isOpen()) {
            LOG.warn("The current connection has been closed, ignoring this send message request");
            return;
        }
        // 将要发送的对象序列化为字节数组
        byte[] sendBytes = new byte[0];
        internalSendMessage(sendBytes);
    }

    /**
     * 内部具体发送消息的实现，交给具体的子类实现
     * @param sendBytes 要发送的字节数组
     */
    protected abstract void internalSendMessage(byte[] sendBytes) throws IOException;
}
