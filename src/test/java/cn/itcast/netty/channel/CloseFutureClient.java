package cn.itcast.netty.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * @author XuHan
 * @date 2023/11/22 15:52
 */
@Slf4j
public class CloseFutureClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                //1.链接服务器
                // connect是一个异步非阻塞的 main线程发起了调用 真正执行连接的的是NIO线程
                .connect("127.0.0.1", 8080);//1s后 建立连接

        Channel channel = channelFuture.sync().channel();
        new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while (true){
                String line = scanner.nextLine();
                if("q".equals(line)){
                    channel.close();//close方法也是一个异步操作 可能是1s之后才真正关闭
                    break;
                }
                channel.writeAndFlush(line);
            }
        },"input").start();

        // 获取一个新的future对象-closeFuture对象 1）同步处理结果 2）异步处理结果
        // ChannelFuture closeFuture = channel.closeFuture();
        // System.out.println("waiting close");
        // closeFuture.sync();
        // log.debug("处理关闭结果");

        //方法2
        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.debug("NIO线程处理关闭结果");
                group.shutdownGracefully();
            }
        });
    }
}
