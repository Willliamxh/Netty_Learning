package cn.itcast.NIO.c5_mutiThread;

import cn.itcast.NIO.c1_buffer.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @author XuHan
 * @date 2023/11/20 10:05
 */
@Slf4j
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        // 主线程 boss 专门监听accept事件
        Thread.currentThread().setName("boss");
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = serverSocketChannel.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        // 1.创建固定数量的worker 并 初始化
        worker worker = new worker("worker-0");
        worker.register();
        while (true){
            boss.select();
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    log.debug("connecting ...{}",socketChannel.getRemoteAddress());
                    //2.关联selector （静态内部类，能访问到私有变量）
                    log.debug("before reg ...{}",socketChannel.getRemoteAddress());
                    // 这边注册会被run方法里面的select阻塞住导致selector不能注册
                    socketChannel.register(worker.selector,SelectionKey.OP_READ);
                    log.debug("after reg ...{}",socketChannel.getRemoteAddress());

                }
            }
        }
    }
    static class worker implements Runnable{
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false;// 开始线程都未启动或者为初始化

        public worker(String name) {
            this.name = name;
        }

        // 初始化线程和selector
        public void register() throws IOException {
            if(!start){
                // 保证我们的worker只有一个线程和一个selector
                // 也就是执行worker run里面的方法
                thread = new Thread(this,name);
                thread.start();
                selector = Selector.open();
                start = true;
            }
        }

        @Override
        public void run() {
            while (true){
                try {
                    // 问题主要是出现在这边  worker的select这边初始化的时候select阻塞住了
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel)key.channel();
                            log.debug("read ...{}",channel.getRemoteAddress());
                            channel.read(buffer);
                            buffer.flip();
                            ByteBufferUtil.debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
