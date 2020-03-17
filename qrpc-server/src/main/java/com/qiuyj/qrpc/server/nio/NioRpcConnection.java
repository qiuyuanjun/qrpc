package com.qiuyj.qrpc.server.nio;

import com.qiuyj.qrpc.cnxn.AbstractRpcConnection;
import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.utils.UnsafeAccess;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * 基于jdk原生的nio的客户端到服务器端之间的连接实现
 * @author qiuyj
 * @since 2020-03-05
 */
class NioRpcConnection extends AbstractRpcConnection {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(NioRpcConnection.class);

    private final SelectionKey selectionKey;

    private final SocketChannel socketChannel;

    /**
     * 报文长度
     */
    private final ByteBuffer messageLength = ByteBuffer.allocate(4);

    /**
     * 报文内容
     */
    private ByteBuffer messageBody;

    /**
     * 从channel里面读取数据时候所需要加的锁
     */
    private final Object readMessageLock = new Object();

    NioRpcConnection(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        this.socketChannel = (SocketChannel) selectionKey.channel();
    }

    /**
     * 处理io任务（包括从客户端读取数据，以及向客户端写入数据）
     * @param selectThread 当前操作所在的{@code SelectThread}
     */
    void handleIO(NioRpcServer.SelectThread selectThread) throws IOException {
        if (!isOpen()) {
            return;
        }
        if (selectionKey.isReadable()) {
            byte[] recvBytes = null;
            synchronized (readMessageLock) {
                int len = -1;
                try {
                    if (Objects.nonNull(messageBody)) {
                        assert messageBody.capacity() == messageLength.getInt();
                        if (messageBody.hasRemaining()) {
                            // 可能是由于tcp的粘包和拆包造成的，此时需要继续读取messageBody
                            len = socketChannel.read(messageBody);
                        }
                    }
                    else {
                        len = socketChannel.read(messageLength);
                        if (len >= 0 && !messageLength.hasRemaining()) {
                            messageBody = ByteBuffer.allocate(messageLength.flip().getInt());
                            len = socketChannel.read(messageBody);
                        }
                    }
                }
                catch (ClosedChannelException e) {
                    resetMessageReadable();
                    return;
                }
                // 如果是客户端关闭了连接，那么服务器端依然能够接收到OP_READ时间，但是读取数据返回的长度会是-1
                // 此时需要服务器端主动关闭和客户端之间的channel
                if (len < 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Current channel {} has been closed", socketChannel.getRemoteAddress());
                    }
                    close();
                }

                if (Objects.nonNull(messageBody) && !messageBody.hasRemaining()) {
                    recvBytes = messageBody.array();
                    resetMessageReadable();
                }
                else if (!messageLength.hasRemaining()) {
                    messageLength.flip();
                }
            }
            if (Objects.nonNull(recvBytes)) {
                // 对字节数组做反序列化操作
                System.out.println("收到客户端消息：" + new String(recvBytes) + ", Thread name: " + Thread.currentThread().getName() + ", SelectThread's name: " + selectThread.getName());
                // 注册当前selector，对OP_WRITE事件感兴趣
//                selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
            }
        }
        else if (selectionKey.isWritable()) {
            System.out.println("write event");
        }
    }

    private void resetMessageReadable() {
        messageBody = null;
        messageLength.clear();
    }

    @Override
    protected void internalSendMessage(byte[] sendBytes) throws IOException {
        ByteBuffer sendBuf = ByteBuffer.allocateDirect(sendBytes.length);
        sendBuf.put(sendBytes);
        try {
            socketChannel.write(sendBuf);
        }
        finally {
            // 清理分配的directBuffer
            UnsafeAccess.getUnsafe().invokeCleaner(sendBuf);
        }
    }

    @Override
    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
        selectionKey.cancel();
    }

    void closeQuietly() {
        try {
            close();
        }
        catch (IOException e) {
            // ignore
        }
    }
}
