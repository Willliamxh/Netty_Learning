package cn.itcast.netty.c5_handler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * @author XuHan
 * @date 2023/11/23 10:53
 */
@Slf4j
public class PipelineServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 1.通过Channel拿到pipeline
                        ChannelPipeline pipeline = ch.pipeline();
                        // 2.添加处理器 head<->h1<->h2<->h3<->tail
                        // inBound
                        pipeline.addLast("h1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("1");
                                ByteBuf buf = (ByteBuf)msg;
                                String name = buf.toString(Charset.defaultCharset());
                                // 执行权由h1传递给h2  将数据传递给下一个handler 如果不调用，调用链会断开
                                super.channelRead(ctx, name);
                            }
                        });
                        pipeline.addLast("h2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object name) throws Exception {
                                log.debug("2");
                                Student student = new Student(name.toString());
                                // 如果这里断开了 3就不会执行了 也就不会执行写了 就只会触发1 2
                                // 这边尝试一下触发写 验证了outbound不受影响
                                // ch.writeAndFlush(ctx.alloc().buffer().writeBytes("server..".getBytes()));
                                super.channelRead(ctx, student);
                            }
                        });
                        pipeline.addLast("h3",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("3，结果：{}，class：{}",msg,msg.getClass());
                                // 往后没有入站处理器了 可以不调用这个了
                                // super.channelRead(ctx, msg);
                                //  * ctx.channel().write(msg) 从尾部开始查找出站处理器
                                // * ctx.write(msg) 是从当前节点找上一个出站处理器
                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("server..".getBytes()));
                                // ch.writeAndFlush(ctx.alloc().buffer().writeBytes("server..".getBytes()));
                            }
                        });
                        // outBound 只有向channel中写了数据才会触发
                        pipeline.addLast("h4",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("4");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h5",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("5");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h6",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("6");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                }).bind(8080);
    }
    static class Student{
        private String name;

        public Student(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Student{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}
