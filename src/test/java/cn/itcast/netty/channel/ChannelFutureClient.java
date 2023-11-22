package cn.itcast.netty.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author XuHan
 * @date 2023/11/22 15:20
 */
@Slf4j
public class ChannelFutureClient {
    public static void main(String[] args) throws Exception {
        // 以后带有Future Promise的类型都是和异步方法配套使用的 用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                //1.链接服务器
                // connect是一个异步非阻塞的 main线程发起了调用 ，
                // 真正执行连接的的是NIO线程
                .connect("127.0.0.1", 8080);//1s后 建立连接

        // 这个时候无阻塞向下执行，获取Channel，但是我这个建立连接要1s，我还没建立连接呢
        // 方法1：使用sync方法同步处理结果（谁发起调用，谁等结果处理结果）
        // channelFuture.sync();// 阻塞住当前线程，等到NIO线程建立好了，才能向下运行。
        // 这边NIO处理链接，主线程等待结果，主线程执行下面的三行代码
        // Channel channel = channelFuture.channel();
        // log.debug("{}",channel);
        // channel.writeAndFlush(new Date() + ": hello world!");

        // 方法2： 使用addListener（回调对象）方法异步处理结果 等结果的也不是我（甩手掌柜）
        // 这边NIO处理链接，链接建立好后调用回调方法，NIO线程自己打印那三行代码。
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            // 在NIO线程连接建立好后，会调用operationComplete
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.channel();
                log.debug("{}",channel);
                channel.writeAndFlush(new Date() + ": hello world!");
            }
        });

    }
}
