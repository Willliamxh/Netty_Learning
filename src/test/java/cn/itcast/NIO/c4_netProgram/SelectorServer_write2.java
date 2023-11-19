package cn.itcast.NIO.c4_netProgram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author XuHan
 * @date 2023/11/19 16:23
 * 可写事件
 */
public class SelectorServer_write2 {
    public static void main(String[] args) throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        // 注册server
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        serverSocketChannel.bind(new InetSocketAddress(8080));

        while (true){
            int select = selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if(selectionKey.isAcceptable()){
                    // 因为只有一个 就这么处理就行了
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    SelectionKey scKey = socketChannel.register(selector, 0, null);
                    // 这边假设我已经有一个关注的事件了，那下面关注写事件的同时还需要关注现在关注的事件
                    scKey.interestOps(SelectionKey.OP_READ);
                    // 1.向客户端发送大量的数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 30000000; i++) {
                        sb.append("a");
                    }
                    // 这个函数直接会切换为读模式
                    ByteBuffer byteBuffer = Charset.defaultCharset().encode(sb.toString());
                    // 2.不能保证一次写入 write 返回实际写入的字节数
                    int write = socketChannel.write(byteBuffer);
                    System.out.println("实际写入字节数 num"+ write);
                    // 3.判断是否存在剩余
                    if(byteBuffer.hasRemaining()){
                        // 4.我的socketChannel关注可写事件，如果之前有关注的事件，还需加上之前关注的事件
                        scKey.interestOps(scKey.interestOps()+SelectionKey.OP_WRITE);
                        // 5.需要把未写完的数据挂在sckey上
                        scKey.attach(byteBuffer);
                    }
                } else if (selectionKey.isWritable()) {
                    // 如果是可写事件
                    ByteBuffer attBytebuffer = (ByteBuffer) selectionKey.attachment();
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    int write = socketChannel.write(attBytebuffer);
                    System.out.println("实际写入字节数 num"+ write);
                    // 6.数据需要清理
                    if(!attBytebuffer.hasRemaining()){
                        // 如果内容写完了 清除buffer
                        selectionKey.attach(null);
                        // 不需要关注可读事件
                        selectionKey.interestOps(selectionKey.interestOps()-SelectionKey.OP_WRITE);
                    }
                }
            }
        }

    }
}
