package itcast.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import itcast.protocol.MessageCodecShareable;
import itcast.protocol.ProtocolFrameDecoder;
import itcast.server.handler.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author XuHan
 * @date 2023/11/28 15:17
 */
@Slf4j
public class ChatServer_xh {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        // 可共享的抽到外面去
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecShareable messageCodec = new MessageCodecShareable();
        LoginRequestMessageHandler loginHandler = new LoginRequestMessageHandler();
        ChatRequestMessageHandler chatHandler = new ChatRequestMessageHandler();
        GroupCreateRequestMessageHandler    groupCreateHandler = new GroupCreateRequestMessageHandler();
        GroupJoinRequestMessageHandler      groupJoinHandler = new GroupJoinRequestMessageHandler();
        GroupMembersRequestMessageHandler   groupMembersHandler = new GroupMembersRequestMessageHandler();
        GroupQuitRequestMessageHandler      groupQuitHandler = new GroupQuitRequestMessageHandler();
        GroupChatRequestMessageHandler      groupChatHandler = new GroupChatRequestMessageHandler();
        QuitHandler quitHandler = new QuitHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 用来判断是不是 读空闲时间过长，或 写空闲时间过长
                    // 5s 内如果没有收到 channel 的数据，会触发一个 IdleState#READER_IDLE 事件
                    ch.pipeline().addLast(new IdleStateHandler(5, 0, 0));
                    // ChannelDuplexHandler 可以同时作为入站和出站处理器（messageCodec也继承了这个）
                    ch.pipeline().addLast(new ChannelDuplexHandler() {
                        // 用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
                            IdleStateEvent event = (IdleStateEvent) evt;
                            // 触发了读空闲事件
                            if (event.state() == IdleState.READER_IDLE) {
                                log.debug("已经 5s 没有读到数据了");
                                ctx.channel().close();
                            }
                        }
                    });
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    ch.pipeline().addLast(loggingHandler);
                    ch.pipeline().addLast(messageCodec);
                    //  我只关心这个message的情况
                    ch.pipeline().addLast(loginHandler);
                    ch.pipeline().addLast(chatHandler);
                    ch.pipeline().addLast(groupCreateHandler);
                    ch.pipeline().addLast(groupJoinHandler);
                    ch.pipeline().addLast(groupMembersHandler);
                    ch.pipeline().addLast(groupQuitHandler);
                    ch.pipeline().addLast(groupChatHandler);
                    ch.pipeline().addLast(quitHandler);
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080);
            log.debug("{} binding...", channelFuture.channel());
            channelFuture.sync();
            log.debug("{} bound...", channelFuture.channel());
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("server error", e);
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            log.debug("stoped");
        }
    }

}
