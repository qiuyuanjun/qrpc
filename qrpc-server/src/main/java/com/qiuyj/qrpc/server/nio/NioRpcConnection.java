package com.qiuyj.qrpc.server.nio;

import com.qiuyj.qrpc.cnxn.RpcConnection;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 基于jdk原生的nio的客户端到服务器端之间的连接实现
 * @author qiuyj
 * @since 2020-03-05
 */
class NioRpcConnection implements RpcConnection {

    private SelectionKey selectionKey;

    private SocketChannel socketChannel;

    NioRpcConnection(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        this.socketChannel = (SocketChannel) selectionKey.channel();
    }

    /**
     * 处理io任务（包括从客户端读取数据，以及向客户端写入数据）
     * @param selectThread 当前操作所在的{@code SelectThread}
     */
    void handlIO(NioRpcServer.SelectThread selectThread) {

    }
}
