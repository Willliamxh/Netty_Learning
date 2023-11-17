package cn.itcast.NIO.c4_netProgram;

import cn.itcast.NIO.c1_buffer.ByteBufferUtil;
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
 * @date 2023/11/17 15:22
 */
@Slf4j
public class SelectorServer_read {
    public static void main(String[] args) throws InterruptedException, IOException {
        // 1.创建selector 管理多个channel
        Selector selector = Selector.open();

        ByteBuffer buffer = ByteBuffer.allocate(10);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        // 2.建立selector和channel的联系 就是注册
        // selectionKey就是事件发生后，得到这个事件是什么事件，哪个channel发生的
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
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();//既有可能是accept事件 也有可能是read事件(因为我们的selector 可以管理ssc和sc)
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                log.debug("key:{}", selectionKey);
                //5.区分事件类型
                if (selectionKey.isAcceptable()) {
                    //事件发生后，要么处理，要么取消（cancel），不能什么都不做，否则下次该事件仍会触发，
                    // 这是因为 nio 底层使用的是水平触发(只要没完成,一直都会触发事件)
                    ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = channel.accept();
                    log.debug("socketChannel:{}", socketChannel);
                    socketChannel.configureBlocking(false);
                    // 注册这个socketChannel
                    SelectionKey socketSelectionKey = socketChannel.register(selector, 0, null);
                    // 我关注read类型
                    socketSelectionKey.interestOps(SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    // 如果是读类型的事件,拿到触发事件的channel,因为读事件一定是socketChannel才会做的 这边做个强转
                    SocketChannel channel = (SocketChannel)selectionKey.channel();
                    int read = channel.read(buffer);
                    buffer.flip();
                    ByteBufferUtil.debugRead(buffer);
                    buffer.clear();
                }
            }
            Thread.sleep(2000);
        }

    }
}
