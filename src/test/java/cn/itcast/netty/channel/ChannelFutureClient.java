package cn.itcast.netty.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
        // channelFuture.sync();
        Channel channel = channelFuture.channel();
        log.debug("{}",channel);
        channel.writeAndFlush(new Date() + ": hello world!");
    }
}
