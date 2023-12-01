package resource_code_learn.c01_start;

import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * @author XuHan
 * @date 2023/12/1 10:31
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        // 1 netty 中使用 NioEventLoopGroup （简称 nio boss 线程）来封装线程和 selector
        Selector selector = Selector.open();

        // 2 创建 NioServerSocketChannel，同时会初始化它关联的 handler，以及为原生 ssc 存储 config
        NioServerSocketChannel attachment = new NioServerSocketChannel();

        // 3 创建 NioServerSocketChannel 时，创建了 java 原生的 ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 非阻塞模式
        serverSocketChannel.configureBlocking(false);

        // 4 启动 nio boss 线程执行接下来的操作

        // 5 注册（仅关联 selector 和 NioServerSocketChannel），未关注事件        注意，我们netty的NioServerSocketChannel作为附件和原生的serverSocketChannel关联起来了
        SelectionKey selectionKey = serverSocketChannel.register(selector, 0, attachment);

        // 6 head -> 初始化器 -> ServerBootstrapAcceptor -> tail，初始化器是一次性的，只为添加 acceptor

        // 7 绑定端口
        serverSocketChannel.bind(new InetSocketAddress(8080));

        // 8 触发 channel active 事件，在 head 中关注 op_accept 事件
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
    }
}
