package cn.itcast.netty.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Date;

/**
 * @author XuHan
 * @date 2023/11/21 10:27
 */
public class HelloClient {
    public static void main(String[] args) throws InterruptedException {
        //1.启动类
        new Bootstrap()
                //2。添加EventLoop
                .group(new NioEventLoopGroup())
                //3.选择客户端Channel
                .channel(NioSocketChannel.class)
                //4.添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override // 链接建立后调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 5.链接到服务器
                .connect(new InetSocketAddress(8080))
                .sync()
                .channel()
                .writeAndFlush(new Date() + ": hello world!"); // 7
    }
}
