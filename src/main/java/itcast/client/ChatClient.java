package itcast.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import itcast.message.LoginRequestMessage;
import itcast.protocol.MessageCodecShareable;
import itcast.protocol.ProtocolFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author XuHan
 * @date 2023/11/29 09:48
 */
@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        // 可共享的抽到外面去
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecShareable messageCodec = new MessageCodecShareable();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                // 接收服务器给我的输入
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    log.debug("message：{}",msg);
                }

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    ch.pipeline().addLast(loggingHandler);
                    ch.pipeline().addLast(messageCodec);
                    ch.pipeline().addLast("clientHandler",new ChannelInboundHandlerAdapter(){
                        // 在链接建立后会触发这个事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 用户输入毕竟是阻塞IO，需要一个独立线程 负责用户在和控制台的输入，向服务器发送消息
                            new Thread(()->{
                                Scanner scanner=new Scanner(System.in);
                                System.out.println("请输入用户名：");
                                String name = scanner.nextLine();
                                System.out.println("请输入密码：");
                                String password = scanner.nextLine();
                                // 构造消息对象
                                LoginRequestMessage loginRequestMessage = new LoginRequestMessage(name, password);
                                // 发送消息 （通过ctx发消息出去，就是从tail往前找handler）
                                ctx.writeAndFlush(loginRequestMessage);

                                System.out.println("等待后续操作");
                                try {
                                    int read = System.in.read();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            },"system in").start();
                        }
                    });



                }
            });
            Channel channel = bootstrap.connect("127.0.0.1", 8080).sync().channel();
            channel.closeFuture().sync();
        }catch (Exception e){
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
