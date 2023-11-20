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
        public void init(SocketChannel socketChannel) throws IOException {
            if(!start){
                // 保证我们的worker只有一个线程和一个selector
                // 也就是执行worker run里面的方法
                thread = new Thread(this,name);
                thread.start();
                selector = Selector.open();
                start = true;
            }
            // 向队列添加任务 但任务并没有执行（在执行这边的代码的时候，work的run可能已经开始运行了，如果我不是先select阻塞住
            // 那我的queue还是空的，也就是没有注册的channel，然后又被阻塞住了。）
            // 所以下面的select必须是在必须在这个任务前面阻塞住。保证我boss线程把任务加进去了，再wakeup进行注册操作。
            // 这么一想 前后其实问题不大，就算是个null被阻塞住了，只要我boss线程加了任务，我就会把selector唤醒。所以问题也不大
            queue.add(()->{
                try {
                    socketChannel.register(selector,SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    throw new RuntimeException(e);
                }
            });
            selector.wakeup();
        }

        @Override
        public void run() {
            while (true){
                try {
                    // 问题主要是出现在这边  worker的select这边初始化的时候select阻塞住了
                    // 用select.wakeup来取消注册
                    selector.select();
                    Runnable task = queue.poll();
                    if(task!=null){
                        task.run();//在这个位置执行了注册操作 boss线程只是添加了任务，任务并没有执行
                    }
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