package com.qiuyj.qrpc.server.nio;

import com.qiuyj.qrpc.cnxn.RpcConnection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * 基于jdk原生的nio的客户端到服务器端之间的连接实现
 * @author qiuyj
 * @since 2020-03-05
 */
class NioRpcConnection implements RpcConnection {

    private SelectionKey selectionKey;

    private SocketChannel socketChannel;

    /**
     * 报文长度
     */
    private final ByteBuffer messageLength = ByteBuffer.allocate(4);

    /**
     * 报文内容
     */
    private ByteBuffer messageBody;

    NioRpcConnection(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        this.socketChannel = (SocketChannel) selectionKey.channel();
    }

    /**
     * 处理io任务（包括从客户端读取数据，以及向客户端写入数据）
     * @param selectThread 当前操作所在的{@code SelectThread}
     */
    void handlIO(NioRpcServer.SelectThread selectThread) throws IOException {
        if (selectionKey.isReadable()) {
            try {
                if (Objects.nonNull(messageBody)) {
                    assert messageBody.capacity() == messageLength.getInt();
                    if (messageBody.hasRemaining()) {
                        // 可能是由于tcp的粘包和拆包造成的，此时需要继续读取messageBody
                        socketChannel.read(messageBody);
                    }
                }
                else {
                    socketChannel.read(messageLength);
                    messageBody = ByteBuffer.allocate(messageLength.flip().getInt());
                    socketChannel.read(messageBody);
                }
            }
            catch (IOException e) {
                resetMessageReadable();
                throw e;
            }
            if (!messageBody.hasRemaining()) {
                byte[] b = messageBody.array();
                resetMessageReadable();
                // 对字节数组做反序列化操作

                // 注册当前selector，对OP_WRITE事件感兴趣

            }
            else {
                messageLength.flip();
            }
        }
        else if (selectionKey.isWritable()) {

        }
    }

    private void resetMessageReadable() {
        messageBody = null;
        messageLength.clear();
    }

    @Override
    public void sendMessage(Object message) {

    }
}
