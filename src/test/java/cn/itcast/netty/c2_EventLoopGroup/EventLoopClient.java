package cn.itcast.netty.c2_EventLoopGroup;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * @author XuHan
 * @date 2023/11/21 10:27
 */
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        //1.启动类   7 第7步
        Channel channel = new Bootstrap()
                //2。添加EventLoop  8 第八步
                .group(new NioEventLoopGroup())
                //3.选择客户端Channel   9 第9步
                .channel(NioSocketChannel.class)
                //4.添加处理器           10 第10步
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override // 链接建立后调用  //12 第12步 链接建立后 调用初始化方法（客户端执行）
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                        // 15.把hello转成byteBuf
                    }
                })
                // 5.链接到服务器     11 第11步
                .connect(new InetSocketAddress(8080))
                // 13.第13步 阻塞方法直到链接建立
                .sync()
                // 客户端和服务器之间的SocketChannel 链接对象
                .channel();
        System.out.println(channel);
        System.out.println("");
    }
}
