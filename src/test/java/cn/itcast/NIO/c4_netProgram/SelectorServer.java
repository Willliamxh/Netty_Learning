package cn.itcast.NIO.c4_netProgram;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;

/**
 * @author XuHan
 * @date 2023/11/17 14:27
 */
@Slf4j
public class SelectorServer {
    public static void main(String[] args) throws InterruptedException, IOException {
        // 1.创建selector 管理多个channel
        Selector selector = Selector.open();

        ByteBuffer buffer = ByteBuffer.allocate(10);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        // 2.建立selector和channel的联系 就是注册
        // selectionKey就是事件发生后，得到这个事件是什么事件，哪个channel发生的
        /**
         绑定的事件类型可以有
         * connect - 客户端连接成功时触发
         * accept - 服务器端成功接受连接时触发
         * ----目前主要关注前面两个事件-----
         * read - 可读事件。数据可读入时触发，有因为接收能力弱，数据暂不能读入的情况
         * write - 可写事件。数据可写出时触发，有因为发送能力弱，数据暂不能写出的情况
         */
        SelectionKey serverSocketSelectionKey = serverSocketChannel.register(selector, 0, null);
        // key只关注accept事件
        serverSocketSelectionKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register key:{}", serverSocketSelectionKey);

        serverSocketChannel.bind(new InetSocketAddress(8080));
        List<SocketChannel> channels = Lists.newArrayList();
        while (true){
            // 我怎么知道有没有发生事件？ 用select方法。没有事件发生，selector阻塞；有事件发生了，线程才向下运行。
            log.info("waiting ...");
            // select在事件未处理时,不会阻塞
            selector.select();
            //4.处理事件。selectionKeySet 内部包含了所有发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                log.debug("key:{}", selectionKey);
                //事件发生后，要么处理，要么取消（cancel），不能什么都不做，否则下次该事件仍会触发，
                // 这是因为 nio 底层使用的是水平触发(只要没完成,一直都会触发事件)
                // ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                // SocketChannel socketChannel = channel.accept();
                // log.debug("socketChannel:{}", socketChannel);
                selectionKey.cancel();
            }
            Thread.sleep(2000);
        }

    }
}
