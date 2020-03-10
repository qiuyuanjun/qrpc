package com.qiuyj.qrpc.server.netty;

import com.qiuyj.qrpc.server.RpcServer;
import com.qiuyj.qrpc.server.RpcServerConfig;
import com.qiuyj.qrpc.service.ServiceDescriptorContainer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 基于netty的rpc服务器的实现
 * @author qiuyj
 * @since 2020-02-29
 */
public class NettyRpcServer extends RpcServer {

    private ServerBootstrap serverBootstrap;

    public NettyRpcServer(RpcServerConfig config, ServiceDescriptorContainer serviceDescriptorContainer) {
        super(config, serviceDescriptorContainer);
    }

    @Override
    protected void internalStart(RpcServerConfig config) {
        serverBootstrap.bind(config.getPort()).syncUninterruptibly();
    }

    @Override
    protected void internalShutdown() {
        serverBootstrap.config().group().shutdownGracefully();
    }

    @Override
    public void configure(RpcServerConfig serverConfig) {
        Class<? extends ServerSocketChannel> channelClass;
        EventLoopGroup parentEventLoopGroup, childEventLoopGroup;
        if (Epoll.isAvailable()) {
            channelClass = EpollServerSocketChannel.class;
            parentEventLoopGroup = new EpollEventLoopGroup(1); // 1条accept线程
            childEventLoopGroup = new EpollEventLoopGroup(); // 采用netty默认的线程数，cpu cores * 2
        }
        else {
            channelClass = NioServerSocketChannel.class;
            parentEventLoopGroup = new NioEventLoopGroup(1); // 1条accept线程
            childEventLoopGroup = new NioEventLoopGroup(); // 采用netty默认的线程数，cpu cores * 2
        }
        serverBootstrap = new ServerBootstrap()
                .channel(channelClass)
                .group(parentEventLoopGroup, childEventLoopGroup)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                    }
                });
    }
}
