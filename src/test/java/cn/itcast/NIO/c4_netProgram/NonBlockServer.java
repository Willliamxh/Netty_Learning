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
 * @date 2023/11/17 13:48
 * 非阻塞模式下的情况。
 * 这个线程有点劳碌命，他一直在循环一直在循环。
 *
 */
@Slf4j
public class NonBlockServer {
    public static void main(String[] args) throws IOException, InterruptedException {

        //0.bytebuffer
        ByteBuffer buffer = ByteBuffer.allocate(10);
        // 1.创建服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设定服务端socket为非阻塞模式，影响的是accept方法
        serverSocketChannel.configureBlocking(false);
        // 2.绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8080));

        // 3.accept 客户端发起请求了，我这边调用了accept 就会建立与客户端的连接 TCP三次握手
        // 考虑到多个客户端接入，多次调用，所以就整个while true循环
        // 再整个连接的集合
        List<SocketChannel> channels = Lists.newArrayList();
        while (true){
            // 4. accept之后socketChannel用来与客户端进行通信
            log.debug("connecting...");
            //非阻塞，没有连接线程还是会继续运行，没有线程来的时候socketChannel是个null
            SocketChannel socketChannel = serverSocketChannel.accept();
            if(socketChannel!=null){
                log.debug("connected... {}",socketChannel);
                // socketChannel也可以设置为非阻塞 影响的是read方法
                socketChannel.configureBlocking(false);
                channels.add(socketChannel);
            }
            // 5.接收客户端发送的数据
            for (SocketChannel channel : channels) {
                log.debug("before read:{}",channel);
                int read = channel.read(buffer);// read现在是非阻塞的了，线程仍然会继续运行。如果没有读到数据，read返回0
                if (read > 0) {
                    buffer.flip();
                    ByteBufferUtil.debugRead(buffer);
                    buffer.clear();
                    log.debug("after read:{}", channel);
                }
            }
            Thread.sleep(2000);
        }

    }
}
