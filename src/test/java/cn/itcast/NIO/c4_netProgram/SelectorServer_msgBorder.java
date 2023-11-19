package cn.itcast.NIO.c4_netProgram;

import cn.itcast.NIO.c1_buffer.ByteBufferUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/**
 * @author XuHan
 * @date 2023/11/19 15:38
 * 处理消息边界
 */
@Slf4j
public class SelectorServer_msgBorder {
    public static void main(String[] args) throws InterruptedException, IOException {
        // 1.创建selector 管理多个channel
        Selector selector = Selector.open();
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
            // 我怎么知道有没有发生事件？
            log.info("waiting ...");
            // 用select方法。没有事件发生，selector阻塞；有事件发生了，线程才向下运行。
            // 事件发生后,select在事件未处理时,不会阻塞.所以事件发生后要么处理,要么取消.
            selector.select();
            //4.处理事件。selectionKeySet 内部包含了所有发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();//既有可能是accept事件 也有可能是read事件(因为我们的selector 可以管理ssc和sc)
            log.info("selectedKeys Sets Size ...{}",selector.selectedKeys().size());
            while (iterator.hasNext()){
                SelectionKey selectedKey = iterator.next();
                log.debug("key:{}", selectedKey);
                //5.区分事件类型
                if (selectedKey.isAcceptable()) {
                    //事件发生后，要么处理，要么取消（cancel），不能什么都不做，否则下次该事件仍会触发，
                    // 这是因为 nio 底层使用的是水平触发(只要没完成,一直都会触发事件)
                    ServerSocketChannel channel = (ServerSocketChannel) selectedKey.channel();
                    SocketChannel socketChannel = channel.accept();
                    log.debug("socketChannel:{}", socketChannel);
                    socketChannel.configureBlocking(false);
                    // 注册这个socketChannel
                    // 把这个buffer放在附件里面，关联到SelectionKey上
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    SelectionKey socketSelectionKey = socketChannel.register(selector, 0, buffer);
                    // 我关注read类型
                    socketSelectionKey.interestOps(SelectionKey.OP_READ);
                    // 处理完这个key之后,要手动移除这个key
                    iterator.remove();
                } else if (selectedKey.isReadable()) {
                    // 如果是读类型的事件,拿到触发事件的channel,因为读事件一定是socketChannel才会做的 这边做个强转
                    SocketChannel channel = (SocketChannel)selectedKey.channel();
                    // 获取附件，也就是我的byteBuffer
                    ByteBuffer attachBuffer = (ByteBuffer)selectedKey.attachment();
                    log.debug(" read socketChannel:{}", channel);
                    int read = channel.read(attachBuffer);
                    log.debug(" read len:{}", read);
                    if (read == -1) {
                        //  如果链接断开,需要在这边做处理.不然下一次进来,那个Channel还是会触发读事件.会默认这个断开的客户端channel的读事件没有处理
                        // https://blog.csdn.net/Wligt/article/details/131650654
                        selectedKey.cancel();
                        continue;
                    }
                    split(attachBuffer);
                    if(attachBuffer.position()==attachBuffer.limit()){
                        // 说明一个都没读取到 我的buffer满了 也没读到换行
                        // 这边先进行扩容
                        ByteBuffer newBuffer = ByteBuffer.allocate(attachBuffer.capacity() * 2);
                        // 然后将旧的读到新的里，切换到读模式
                        attachBuffer.flip();
                        newBuffer.put(attachBuffer);
                        selectedKey.attach(newBuffer);

                    }
                    iterator.remove();
                }
            }
            Thread.sleep(2000);
        }
    }


    private static void split(ByteBuffer source) {
        //切换到读模式
        source.flip();
        //遍历直到找到分隔符
        for (int i = 0; i < source.limit(); i++) {
            if(source.get(i)=='\n'){
                //把这条信息存入到一个新的buffer
                //先计算这个消息的长度 i+1-起始索引
                int len = i+1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(len);
                // 写入target
                for (int j = 0; j < len; j++) {
                    target.put(source.get());
                }
                target.flip();
                System.out.println(StandardCharsets.UTF_8.decode(target).toString());
                // System.out.println(Arrays.toString(StandardCharsets.UTF_8.decode(target).toString().getBytes(StandardCharsets.UTF_8)));
                // 这个工具类会把\n替换为.
                ByteBufferUtil.debugAll(target);
            }
        }

        //切换回写模式
        source.compact(); // compact 让postion到剩余未读的位置（我一个都没读）。postion 16 limit 16 就是说明我需要扩容
    }

}
