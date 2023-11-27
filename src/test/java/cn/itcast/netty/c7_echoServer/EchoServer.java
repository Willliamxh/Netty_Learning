package cn.itcast.netty.c7_echoServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.Charset;

public class EchoServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                ByteBuf buffer = (ByteBuf) msg;
                                System.out.println(buffer.toString(Charset.defaultCharset()));

                                // 建议使用 ctx.alloc() 创建 ByteBuf
                                ByteBuf response = ctx.alloc().buffer();
                                response.writeBytes(buffer);
                                ctx.writeAndFlush(response);

                                // 思考：需要释放 buffer 吗
                                buffer.release();
                                // 思考：需要释放 response 吗
                                response.release();
                            }
                        });
                    }
                }).bind(8080);
    }
}
//取决于你重写的channelRead方法，如果向下传递则可以不释放，不用向后传递说明是最后调用的方法，所以需要释放

/**
 * 入站 ByteBuf 处理原则
    对原始 ByteBuf 不做处理，调用 ctx.fireChannelRead(msg) 向后传递，这时无须 release
    将原始 ByteBuf 转换为其它类型的 Java 对象，这时 ByteBuf 就没用了，必须 release
    如果不调用 ctx.fireChannelRead(msg) 向后传递，那么也必须 release
    注意各种异常，如果 ByteBuf 没有成功传递到下一个 ChannelHandler，必须 release
    假设消息一直向后传，那么 TailContext 会负责释放未处理消息（原始的 ByteBuf）
 * 出站 ByteBuf 处理原则
    出站消息最终都会转为 ByteBuf 输出，一直向前传，由 HeadContext flush 后 release

 */