package cn.itcast.netty.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author XuHan
 * @date 2023/11/21 10:08
 */
public class HelloServer {
    public static void main(String[] args) {
        //1.服务器端启动器 功能是负责组装netty组件 ，启动服务器。
        new ServerBootstrap()
                // 2.bossEventLoop workerEventLoop（可以翻译为循环处理事件）。可以简单理解为 `线程池 + Selector`
                .group(new NioEventLoopGroup())
                // 3.选择服务器的serverSocketChannel实现
                .channel(NioServerSocketChannel.class)
                // 4.这边的child和worker是一个意思。boss是处理链接的，worker是处理读写的。
                // child就是我们的worker（child）是要执行哪些操作handler
                .childHandler(
                        //5.Channel代表客户端进行数据读写通道 initializer的初始化。负责添加别的Handler
                        //为啥方法叫 childHandler，是接下来添加的处理器都是给 SocketChannel 用的，而不是给 ServerSocketChannel。
                        // ChannelInitializer 处理器（仅执行一次），它的作用是待客户端 SocketChannel 建立连接后，执行 initChannel 以便添加更多的处理器
                        new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //6.添加具体的Handler
                        ch.pipeline().addLast(new StringDecoder());//将byteBuf转化为字符串
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){//自定义的Handler
                            @Override //读事件发生之后
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                //打印上一步转化的字符串
                                System.out.println(msg);;
                            }
                        });
                    }
                }).bind(8080);
    }
}
