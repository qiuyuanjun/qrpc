package com.qiuyj.qrpc.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.concurrent.CountDownLatch;

/**
 * @author qiuyj
 * @since 2020-03-14
 */
public class RpcClientTest {

    public static void main(String[] args) throws InterruptedException {
        Bootstrap b = new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(new NioEventLoopGroup())
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new MessageToByteEncoder<String>() {

                            @Override
                            protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
                                byte[] b = msg.getBytes();
                                out.writeInt(b.length);
                                out.writeBytes(b);
                                System.out.println("send successfully");
                            }
                        });
                    }
                });
        ChannelFuture f = b.connect("127.0.0.1", 11221).syncUninterruptibly();
        CountDownLatch countDownLatch = new CountDownLatch(1000);
        for (int i = 1; i <= 1000; i++) {
            f.channel().writeAndFlush("Hello rpc server, this is rpc client's message of seria number: " + i)
                    .addListener(future -> countDownLatch.countDown());
        }
        countDownLatch.await();
        b.config().group().shutdownGracefully();
        f.channel().close();
    }
}
