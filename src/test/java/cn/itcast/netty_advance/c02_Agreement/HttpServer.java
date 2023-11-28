package cn.itcast.netty_advance.c02_Agreement;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * @author XuHan
 * @date 2023/11/28 10:10
 */
@Slf4j
public class HttpServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new HttpServerCodec());
                    // ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    //     @Override
                    //     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    //         log.debug("{}", msg.getClass());
                    //         // 会解析成两段 DefaultHttpRequest 请求头
                    //         if (msg instanceof HttpRequest) { // 请求行，请求头
                    //
                    //         }
                    //         // LastHttpContent$1 请求体
                    //         else if (msg instanceof HttpContent) { //请求体
                    //
                    //         }
                    //     }
                    // });
                    // 只关心某一种特定的请求 只有HttpRequest 请求才会被处理
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
                            // 获取请求行 请求头
                            log.debug(msg.uri());
                            // log.debug(String.valueOf(msg.headers()));

                            // 返回响应
                            DefaultFullHttpResponse response =
                                    new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);

                            byte[] bytes = "<h1>Hello, world!</h1>".getBytes();
                            // 如果不加这个长度，浏览器会一直等待更多的信息
                            response.headers().setInt(CONTENT_LENGTH, bytes.length);
                            response.content().writeBytes(bytes);

                            // 写回响应
                            ctx.writeAndFlush(response);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
//前面别误导人了，就是长链接导致的问题，如果是短链接，发送完就关闭了，
// 浏览器认为数据接收完了，但是长连接下不给长度标识是浏览器无法感知到底数据接收完没有，不懂还乱bb