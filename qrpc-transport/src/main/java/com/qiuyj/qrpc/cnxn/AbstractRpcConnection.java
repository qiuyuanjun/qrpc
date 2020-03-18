package com.qiuyj.qrpc.cnxn;

import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.message.Message;
import com.qiuyj.qrpc.message.MessageConverters;
import com.qiuyj.qrpc.message.MessageUtils;
import com.qiuyj.qrpc.message.payload.RpcRequest;
import com.qiuyj.qrpc.message.payload.RpcResult;

import java.io.IOException;

/**
 * @author qiuyj
 * @since 2020-03-15
 */
public abstract class AbstractRpcConnection implements RpcConnection {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(AbstractRpcConnection.class);

    private final MessageConverters messageConverters;

    protected AbstractRpcConnection(MessageConverters messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Override
    public void sendMessage(Object message) throws IOException {
        if (!isOpen()) {
            LOG.warn("The current connection has been closed, ignoring this send message request");
            return;
        }
        // 将要发送的对象序列化为字节数组
        Message sendMsg;
        if (message instanceof Message) {
            sendMsg = (Message) message;
        }
        else if (message instanceof RpcRequest) {
            // 请求对象
            sendMsg = MessageUtils.requestMessage((RpcRequest) message);
        }
        else if (message instanceof RpcResult) {
            // 响应对象
            sendMsg = MessageUtils.resultMessage((RpcResult) message);
        }
        else {
            // 如果上述都不是，那么我们默认是响应对象（需要构建）
            RpcResult result = new RpcResult();
            sendMsg = MessageUtils.resultMessage(result);
        }
        byte[] sendBytes = messageConverters.fromMessage(sendMsg);
        internalSendMessage(sendBytes);
    }

    /**
     * 内部具体发送消息的实现，交给具体的子类实现
     * @param sendBytes 要发送的字节数组
     */
    protected abstract void internalSendMessage(byte[] sendBytes) throws IOException;
}
