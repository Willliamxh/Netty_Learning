package cn.itcast.netty.c7_echoServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;
import java.util.Scanner;

public class EchoClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buffer = (ByteBuf) msg;
                                System.out.println(buffer.toString(Charset.defaultCharset()));

                                // 思考：需要释放 buffer 吗
                            }
                        });
                    }
                }).connect("127.0.0.1", 8080).sync().channel();
        channel.closeFuture().addListener(future -> {
            group.shutdownGracefully();
        });
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close();
                    break;
                }
                channel.writeAndFlush(line);
            }
        }).start();
    }
}
//正解:对于ByteBuf来说，分为池化（Pooled）和非池化（Unpooled）两种分配内存的方式。其中UnpooledHeapByteBuf 底下的byte[]能够依赖JVM GC自然回收，
//这题我会，就算是这边没有进行accept，客户端connect了，也可以成功建立通道，因为是tcp是面向连接
//需要释放buffer而不需要释放response