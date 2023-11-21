package cn.itcast.netty.c2_EventLoopGroup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * @author XuHan
 * @date 2023/11/21 13:59
 * 职责进一步划分
 */
@Slf4j
public class EventLoopServerBossWorker {
    public static void main(String[] args) {
        // 整一个defaultEventLoopGroup 处理耗时长的事件
        DefaultEventLoop defaultEventLoop = new DefaultEventLoop();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // boss只负责serverSocketChannel的accept事件  worker SocketChannel负责读写操作
        // boss可以不设置线程，因为我服务器就一个，ServerSocketChannel只会绑定到NioEventLoopGroup的一个线程上
        // worker按需设置线程数 这边设置两个线程
        serverBootstrap.group(new NioEventLoopGroup(),new NioEventLoopGroup(2));
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override                                               // bytebuf
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        ByteBuf byteBuf = (ByteBuf) msg;
                        log.debug(byteBuf.toString(Charset.defaultCharset()));
                        ctx.fireChannelRead(msg);
                    }
                }).addLast(defaultEventLoop,"handler2",new ChannelInboundHandlerAdapter() {
                    @Override                                               // bytebuf
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        ByteBuf byteBuf = (ByteBuf) msg;
                        log.debug(byteBuf.toString(Charset.defaultCharset()));
                    }
                });
            }
        }).bind(8080);
    }
}
