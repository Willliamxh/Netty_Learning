package cn.itcast.NIO.c4_netProgram;

import cn.itcast.NIO.c1_buffer.ByteBufferUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * @author XuHan
 * @date 2023/11/17 13:11
 * 使用NIO来理解阻塞模式，单线程
 */
@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {

        //0.bytebuffer
        ByteBuffer buffer = ByteBuffer.allocate(10);
        // 1.创建服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 2.绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8080));

        // 3.accept 客户端发起请求了，我这边调用了accept 就会建立与客户端的连接 TCP三次握手
        // 考虑到多个客户端接入，多次调用，所以就整个while true循环
        // 再整个连接的集合
        List<SocketChannel> channels = Lists.newArrayList();
        while (true){
            // 4. accept之后socketChannel用来与客户端进行通信
            log.debug("connecting...");
            SocketChannel socketChannel = serverSocketChannel.accept();//阻塞方法，线程阻塞住
            log.debug("connected... {}",socketChannel);
            channels.add(socketChannel);
            // 5.接收客户端发送的数据
            for (SocketChannel channel : channels) {
                log.debug("before read:{}",channel);
                channel.read(buffer);// read方法也是个阻塞的，线程被阻塞。没有数据过来，read就干等
                buffer.flip();
                ByteBufferUtil.debugRead(buffer);
                buffer.clear();
                log.debug("after read:{}",channel);
            }

        }

    }
}
