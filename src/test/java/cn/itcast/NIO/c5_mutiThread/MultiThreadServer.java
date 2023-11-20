package cn.itcast.NIO.c5_mutiThread;

import cn.itcast.NIO.c1_buffer.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

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
        // 1.创建固定数量的worker 并
        worker worker = new worker("worker-0");
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
                    worker.init(socketChannel);// 初始化 worker 被boss线程调用
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
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        public worker(String name) {
            this.name = name;
        }

        // 初始化线程和selector
        public void init(SocketChannel socketChannel) throws Exception {
            if(!start){
                // 保证我们的worker只有一个线程和一个selector
                // 也就是执行worker run里面的方法
                selector = Selector.open();
                thread = new Thread(this,name);
                thread.start();
                start = true;
            }
            // wakeup 简单实现，wakeup相当于是给了一张门票。先给票，下次要阻塞了发现有票，就不阻塞了
            // 实测会出现问题，问题原因是之前把Selector.open();放在了thread.start();之后，导致selector出现null值。已经解决
            // 然后抛出异常
            selector.wakeup();
            socketChannel.register(selector,SelectionKey.OP_READ);


        }

        @Override
        public void run() {
            while (true){
                try {
                    // 问题主要是出现在这边  worker的select这边初始化的时候select阻塞住了
                    // 用select.wakeup来取消注册
                    log.debug(" selector.select();{}",selector);
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
//还是不明白，为啥非要在select后register
// 如果当前Selector没有阻塞在select方法上，那么本次 wakeup调用会在下一次select阻塞的时候生效
//为什么要唤醒？就是为了注册读事件！
// 所以大致意思是利用select阻塞住worker，
// 而将来根据main线程在合适的时机放行worker，register方法隐含了两个语义，worker准备好了，main线程也确保事件到来了

//如果注册放到上面，极大概率队列取出的是空，然后又阻塞了

//如果两个请求，第一个请求worker-0执行到78行拿到任务了，
// 在读取数据的时候第二个请求添加进队列并唤醒，那执行完第一个任务的数据读取之后，还是阻塞住了，第二个任务没法去执行注册
//当然不能放在上面了，万一run线程跑的快，你这个queue都还没有添加进去，run里面拿出来是null,必须先阻塞，放了任务再wakeup，
// 应该是selec阻塞中,再register不会立刻生效 所以要wakeup再注册再调用select()
//多个线程直之间哪个线程先执行是不确定的