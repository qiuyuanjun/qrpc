package com.qiuyj.qrpc.server.nio;

import com.qiuyj.qrpc.QrpcThread;
import com.qiuyj.qrpc.cnxn.RpcConnection;
import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.message.MessageConverters;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于nio的rpc服务器的实现，参考了zookeeper的NioServerCnxnFactory的实现
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
     * 专门用于处理read或者write事件的线程（Math.sqrt(coreNums / 2)）
     */
    private List<SelectThread> selectThreads;

    private ServerSocketChannel ss;

    /**
     * 专门用于处理IO操作的线程池（coreNums * 2）
     */
    private ExecutorService workerPool;
    private AtomicInteger workerPoolThreadCount = new AtomicInteger();

    public NioRpcServer(RpcServerConfig config,
                        ServiceDescriptorContainer serviceDescriptorContainer,
                        MessageConverters messageConverters) {
        super(config, serviceDescriptorContainer, messageConverters);
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

        if (LOG.isInfoEnabled()) {
            LOG.info("NioRpcServer has been started at: {}", ss.socket().getLocalSocketAddress());
        }
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

        workerPool.shutdown();
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
        int workerNums = numCores << 1;

        if (LOG.isDebugEnabled()) {
            LOG.debug("numCores: {}, selectThreadNums: {}, workerNums: {}", numCores, selectThreadNums, workerNums);
        }

        SelectThread[] selectThreads = new SelectThread[selectThreadNums];
        for (int i = 0; i < selectThreadNums; i++) {
            selectThreads[i] = new SelectThread(i + 1);
        }
        this.selectThreads = List.of(selectThreads);
        acceptThread = new AcceptThread(this.selectThreads);
        workerPool = new ThreadPoolExecutor(workerNums,
                workerNums,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(Math.max(workerNums, 64)),
                r -> new QrpcThread(r, "NioWorker-" + workerPoolThreadCount.incrementAndGet()));
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        try {
            return (InetSocketAddress) ss.getLocalAddress();
        }
        catch (IOException e) {
            return null;
        }
    }

    private abstract static class AbstractSelectorThread extends QrpcThread {

        Selector selector;

        AbstractSelectorThread(String name) {
            super(name);
        }

        /**
         * 初始化selector
         */
        protected void initSelector() {
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

        private void cancelSelectionKeys() {
            selector.selectedKeys().forEach(SelectionKey::cancel);
        }

        @Override
        public void run() {
            // 初始化selector
            initSelector();
            try {
                mainLoop();
            }
            finally {
                // 退出了循环，那么不管是正常退出，还是循环内抛出了异常，那么均需要执行清理资源的方法
                cancelSelectionKeys();
                closeSelector();
            }
        }

        protected abstract void mainLoop();
    }

    private class AcceptThread extends AbstractSelectorThread {

        private List<SelectThread> selectThreads;

        private int selectThreadCursor = -1;

        private SelectionKey acceptSk;

        private AcceptThread(List<SelectThread> selectThreads) {
            super("NioAccept");
            this.selectThreads = Collections.unmodifiableList(selectThreads);
        }

        @Override
        protected void initSelector() {
            super.initSelector();
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
        protected void mainLoop() {
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
        }

        private void acceptSocketChannel() throws IOException {
            int n = selector.select();
            if (LOG.isDebugEnabled()) {
                LOG.debug("The current acceptThread's selector has received {} ACCEPT event keys", n);
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
                    NioUtils.cancelSelectionKey(sk);
                }
                else if (sk.isAcceptable()) {
                    if (!doAccept()) {
                        pauseAccept();
                    }
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
                SelectThread st = roundRobinGetSelectThread();
                if (!st.acceptSocketChannel(sc)) {
                    // 表明rpc服务器已经关闭了，那么抛出异常
                    throw new IOException("Accept socket channel to select thread: " + st.getName() + " failed");
                }
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

    class SelectThread extends AbstractSelectorThread {

        private Queue<SocketChannel> acceptSocketChannelQueue = new ConcurrentLinkedQueue<>();

        private SelectThread(int i) {
            super("NioSelect-" + i);
        }

        @Override
        protected void mainLoop() {
            while (isRunning() && ss.isOpen()) {
                try {
                    select();
                    processAcceptSocketChannel();
                }
                catch (RuntimeException e) {
                    LOG.warn("Ignore runtime exception", e);
                }
                catch (Exception e) {
                    LOG.warn("Ignore exception", e);
                }
            }
        }

        private void select() throws IOException {
            int n = selector.select();
            if (LOG.isDebugEnabled()) {
                LOG.debug("The current selectThread [{}]'s selector has received {} READ or WRITE event keys", getName(), n);
            }

            Iterator<SelectionKey> skIter = selector.selectedKeys().iterator();
            while (skIter.hasNext()) {
                SelectionKey sk = skIter.next();
                skIter.remove();
                if (!sk.isValid()) {
                    NioUtils.cancelSelectionKey(sk);
                }
                else if (sk.isReadable() || sk.isWritable()) {
                    // 处理io操作
                    handlIO(sk);
                }
                else {
                    LOG.warn("The current SelectionKey: {} is not READ or WRITE ops", sk);
                }
            }
        }

        private void handlIO(SelectionKey sk) {
            RpcConnection attachment = (RpcConnection) sk.attachment();
            if (!(attachment instanceof NioRpcConnection)) {
                throw new IllegalStateException("Not an NioRpcConnection: " + attachment);
            }
            NioRpcConnection conn = (NioRpcConnection) attachment;
            if (conn.isOpen()) {
                // TODO 对连接做一些必要的设置
                // 将io处理任务提交到worker线程
                workerPool.execute(() -> {
                    try {
                        conn.handlIO(SelectThread.this);
                    }
                    catch (IOException e) {
                        // 抛出IO异常，一般是客户端被动关闭了channel（比如强制kill客户端的进程）
                        // 此时，服务器端也要同步关闭掉和客户端连接的channel
                        conn.closeQuietly();
                        LOG.error("Unexpected IO exception while handle reading or writing operations", e);
                    }
                });
            }
        }

        private void processAcceptSocketChannel() {
            SocketChannel sc;
            while (isRunning() && Objects.nonNull(sc = acceptSocketChannelQueue.poll())) {
                SelectionKey sk;
                try {
                    sc.setOption(StandardSocketOptions.TCP_NODELAY, true);
                    sc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                    sk = sc.register(selector, SelectionKey.OP_READ);
                    RpcConnection conn = new NioRpcConnection(sk);
                    sk.attach(conn);
                }
                catch (IOException e) {
                    // 忽略异常信息
                    NioUtils.closeSocketChannelQuietly(sc);
                }
            }
        }

        boolean acceptSocketChannel(SocketChannel sc) {
            boolean accept = true;
            if (!isRunning() || !acceptSocketChannelQueue.offer(sc)) {
                accept = false;
            }
            wakeupSelector();
            return accept;
        }
    }
}
