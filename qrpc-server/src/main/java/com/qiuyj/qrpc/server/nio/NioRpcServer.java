package com.qiuyj.qrpc.server.nio;

import com.qiuyj.qrpc.QrpcThread;
import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.server.RpcServer;
import com.qiuyj.qrpc.server.RpcServerConfig;
import com.qiuyj.qrpc.service.ServiceDescriptorContainer;
import com.qiuyj.qrpc.utils.NioUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 基于nio的rpc服务器的实现，参考了zookeeper的ServerCnxnFactoryNio的实现
 * @author qiuyj
 * @since 2020-02-29
 */
public class NioRpcServer extends RpcServer {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(NioRpcServer.class);

    /**
     * 接收线程，专门用于接收客户端的连接
     */
    private AcceptThread acceptThread;

    /**
     * 专门用于处理read或者write事件的线程
     */
    private List<SelectThread> selectThreads;

    private ServerSocketChannel ss;

    public NioRpcServer(RpcServerConfig config, ServiceDescriptorContainer serviceDescriptorContainer) {
        super(config, serviceDescriptorContainer);
    }

    @Override
    protected void internalStart(RpcServerConfig config) {
        // 如果启动rpcServer的时候，ss为null
        // 表明当前rpcServer没有调用configure方法，那么就需要手动调用
        if (Objects.isNull(ss)) {
            configure(config);
        }

        try {
            ss.bind(new InetSocketAddress(config.getPort()));
        }
        catch (IOException e) {
            throw new IllegalStateException("Binding local port to the server socket channel error", e);
        }

        // 启动各种线程
        acceptThread.start();
        selectThreads.forEach(Thread::start);
    }

    @Override
    protected void internalShutdown() {
        NioUtils.closeServerSocketChannelQuietly(ss);

        // 清理各种线程的资源
        if (acceptThread.isAlive()) {
            acceptThread.wakeupSelector();
            try {
                acceptThread.join();
            }
            catch (InterruptedException e) {
                LOG.warn("AcceptThread has been interrupted", e);
            }
        }
        for (SelectThread selectThread : selectThreads) {
            if (selectThread.isAlive()) {
                selectThread.wakeupSelector();
                try {
                    selectThread.join();
                }
                catch (InterruptedException e) {
                    LOG.warn("SelectThread: " + selectThread.getName() + " has been interrupted", e);
                }
            }
        }
    }

    @Override
    public void configure(RpcServerConfig config) {
        try {
            ss = ServerSocketChannel.open();
            ss.configureBlocking(false); // 非阻塞
            ss.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        }
        catch (IOException e) {
            throw new IllegalStateException("Error opening and configuring server socket channel", e);
        }

        int numCores = Runtime.getRuntime().availableProcessors();
        int selectThreadNums = (int) Math.sqrt(numCores >>> 1);
//        int workerNums = numCores << 1;
        selectThreads = new ArrayList<>(selectThreadNums);
        for (int i = 0; i < selectThreadNums; i++) {
            selectThreads.set(i, new SelectThread(i));
        }
        acceptThread = new AcceptThread(selectThreads);
    }

    private abstract static class AbstractSelectorThread extends QrpcThread {

        Selector selector;

        AbstractSelectorThread(String name) {
            super(name);
        }

        /**
         * 初始化selector
         */
        void initSelector() {
            try {
                selector = Selector.open();
            }
            catch (IOException e) {
                throw new IllegalStateException("Error opening selector", e);
            }
        }

        /**
         * 唤醒正在等待的selector
         */
        void wakeupSelector() {
            selector.wakeup();
        }

        /**
         * 关闭当前Selector
         */
        void closeSelector() {
            NioUtils.closeSelectorQuietly(selector);
        }

        void cancelSelectionKeys() {
            selector.selectedKeys().forEach(SelectionKey::cancel);
        }
    }

    private class AcceptThread extends AbstractSelectorThread {

        private List<SelectThread> selectThreads;

        private int selectThreadCursor = -1;

        private SelectionKey acceptSk;

        private AcceptThread(List<SelectThread> selectThreads) {
            super("NIO-Accept");
            this.selectThreads = Collections.unmodifiableList(selectThreads);

            // 将当前AcceptThread的Selector注册到ServerSocketChannel上，并接受ACCEPT事件
            try {
                this.acceptSk = ss.register(selector, SelectionKey.OP_ACCEPT);
            }
            catch (ClosedChannelException e) {
                closeSelector();

                throw new IllegalStateException("Error registering selector on ServerSocketChannel. " +
                        "It is possible that ServerSocketChannel has been closed", e);
            }
        }

        @Override
        public void run() {
            initSelector();
            while (isRunning() && ss.isOpen()) {
                try {
                    acceptSocketChannel();
                }
                catch (RuntimeException e) {
                    LOG.warn("Ignore runtime exception", e);
                }
                catch (Exception e) {
                    LOG.warn("Ignore exception", e);
                }
            }
            cancelSelectionKeys();
            closeSelector();
        }

        private void acceptSocketChannel() throws IOException {
            int n = selector.select();
            if (LOG.isDebugEnabled()) {
                LOG.debug("The current selector has received {} ACCEPT event keys", n);
            }

            processSelectedAcceptKeys(selector.selectedKeys());
        }

        private void processSelectedAcceptKeys(Set<SelectionKey> selectionKeys) {
            if (selectionKeys.isEmpty()) {
                return;
            }
            Iterator<SelectionKey> skIter = selectionKeys.iterator();
            while (skIter.hasNext()) {
                SelectionKey sk = skIter.next();
                skIter.remove();
                if (!sk.isValid()) { // 无效的selectionKey，直接忽略
                    continue;
                }
                if (sk.isAcceptable() && !doAccept()) {
                    pauseAccept();
                }
                else {
                    LOG.warn("The current SelectionKey: {} is not ACCEPT ops", sk);
                }
            }
        }

        private void pauseAccept() {
            // interestOps设置为0，表示对任何事件都不感兴趣
            acceptSk.interestOps(0);
            try {
                selector.select(10); // 等待10毫秒
            }
            catch (IOException e) {
                LOG.warn("Ignore the exception that wait and then select the selection key set", e);
            }
            acceptSk.interestOps(SelectionKey.OP_ACCEPT);
        }

        private boolean doAccept() {
            boolean accept = false;
            SocketChannel sc = null;
            try {
                sc = ss.accept();
                sc.configureBlocking(false);
                roundRobinGetSelectThread().acceptSocketChannel(sc);
                accept = true;
            }
            catch (IOException e) {
                LOG.warn("Ignore the exception that failed to accept socket channel", e);
                NioUtils.closeSocketChannelQuietly(sc);
            }
            return accept;
        }

        private SelectThread roundRobinGetSelectThread() {
            int i = ++selectThreadCursor;
            if (i >= selectThreads.size()) {
                i = selectThreadCursor = 0;
            }
            return selectThreads.get(i);
        }
    }

    private class SelectThread extends AbstractSelectorThread {

        private SelectThread(int i) {
            super("NIO-Select-" + i);
        }

        @Override
        public void run() {
            initSelector();
            while (isRunning() && ss.isOpen()) {

            }
        }

        void acceptSocketChannel(SocketChannel sc) {

        }
    }
}
