package itcast.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import itcast.message.RpcRequestMessage;
import itcast.protocol.MessageCodecShareable;
import itcast.protocol.ProtocolFrameDecoder;
import itcast.protocol.SequenceIdGenerator;
import itcast.server.handler.RpcResponseMessageHandler;
import itcast.server.service.HelloService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * @author XuHan
 * @date 2023/11/30 14:26
 */
@Slf4j
public class RPCClientManagerXH {
    public static void main(String[] args) {
        HelloService service = getProxyService(HelloService.class);
        service.sayHello("hello！！！！");
        service.sayHello("你好呀！！！！");
        service.sayHello("哈哈哈哈哈哈！！！！");
    }

    /**
     *
     * @param serviceClass
     * @return
     * @param <T>
     */
    public static <T> T getProxyService(Class<T> serviceClass){
        ClassLoader loader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};
        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            //1.将方法的调用转化为消息对象
            RpcRequestMessage message = new RpcRequestMessage(
                    SequenceIdGenerator.nextId(),
                    serviceClass.getName(),
                    method.getName(),
                    // 返回值
                    method.getReturnType(),
                    // 参数类型
                    method.getParameterTypes(),
                    // 参数值
                    args
            );

            //2.将消息对象发送出去
            getChannel().writeAndFlush(message);
            //3。暂时先返回一个null
            return null;
        });

        return (T) o;
    }

    private static Channel channel = null;
    private static final Object LOCK = new Object();

    //获取唯一的Channel对象
    public static Channel getChannel(){
        if(channel!=null){
            return channel;
        }
        synchronized (LOCK){
            if(channel!=null){
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    // 初始化Channel
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecShareable MESSAGE_CODEC = new MessageCodecShareable();

        // rpc 响应消息处理器，待实现
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProtocolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC);
                ch.pipeline().addLast(RPC_HANDLER);
            }
        });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            // 这里必须改成异步 不然我一直阻塞等着我的Channel关闭 Channel对象都返回不了了
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}
//new 对象操作不是原子的，可能返回了指针但是对象还未初始化，这时==null为false，但实际对象还未初始化，volatile可以保证指令不重排，保证new返回的对象一定是已经初始化的