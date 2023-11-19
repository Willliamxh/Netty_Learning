package cn.itcast.NIO.c4_netProgram;

import cn.itcast.NIO.c1_buffer.ByteBufferUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

/**
 * @author XuHan
 * @date 2023/11/17 15:22
 * 红色是所有key的集合 keys集合
 * 绿色是发生事件的集合, selectedKeys 集合 是
 * 发生事件集合的key不会主动删除,得手动删除,所以只能用迭代器
 * sun.nio.ch.SelectorImpl里面有这两个对象
 */
@Slf4j
public class SelectorServer_read {
    public static void main(String[] args) throws InterruptedException, IOException {
        // 1.创建selector 管理多个channel
        Selector selector = Selector.open();

        ByteBuffer buffer = ByteBuffer.allocate(4);
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
                    SelectionKey socketSelectionKey = socketChannel.register(selector, 0, null);
                    // 我关注read类型
                    socketSelectionKey.interestOps(SelectionKey.OP_READ);
                    // 处理完这个key之后,要手动移除这个key
                    iterator.remove();
                } else if (selectedKey.isReadable()) {
                    // 如果是读类型的事件,拿到触发事件的channel,因为读事件一定是socketChannel才会做的 这边做个强转
                    SocketChannel channel = (SocketChannel)selectedKey.channel();
                    log.debug(" read socketChannel:{}", channel);
                    int read = channel.read(buffer);
                    log.debug(" read len:{}", read);
                    if (read == -1) {
                        //  如果链接断开,需要在这边做处理.不然下一次进来,那个Channel还是会触发读事件.会默认这个断开的客户端channel的读事件没有处理
                        // https://blog.csdn.net/Wligt/article/details/131650654
                        selectedKey.cancel();
                        continue;
                    }
                    buffer.flip();
                    System.out.println(Charset.defaultCharset().decode(buffer).toString());
                    ByteBufferUtil.debugRead(buffer);
                    buffer.clear();
                    iterator.remove();
                }
            }
            Thread.sleep(2000);
        }

    }
}
/**
 * 是因为read没处理，虽然在刚进循环被从集合中移除，但由于没处理，所以又被加入集合中导致的循环。不是前面说的移除的另外一个集合
 * remove删除的是上节课的绿框里面已经处理过的事件,现在是这个链接事件已经不需要了,所以将红框里面的也删除了
 * 不光selectionKey要删除，具体的事件也要处理（handle）
 猜测是因为断开连接 会向服务端channel发送一个readble消息。

 断开连接相当于一个read事件
 因为你的事件没有处理 虽然在selectKey里删掉了 但是下次调用select方法又会放入到selectdKey的集合里面


 因为客户端断开，会引发read事件，而且只要key不取消注册，每次select都会找到这个时间，就会进到read分支

 断开链接会发报文的，而且是传输层（操作系统）实现的，和你没关系，建议多点知识储备再来学，否则事倍功半

 看两遍总结：selector监听read事件是否处理。iter的key在remove后，从selector中取的key仍然会包含这个read。因为这个read事件没有真正处理掉。


 https://blog.csdn.net/Wligt/article/details/131650654
 这个是NIO框架的一个bug，客户端断开会一直触发read

 当客户端主动连接断开时，为了让服务器知道断开了连接，会产生OP_READ事件.
 然后这个事件,不是靠读就算处理的


 当客户端断开连接，那么将会触发读就绪，并且channel的read()方法返回-1，表示连接已断开，服务器应该要做出处理，关闭这个连接。

 我的 mac 没抛异常,判断下 read 的结果小于 0 取消吧
 */