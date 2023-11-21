package cn.itcast.netty.c2_EventLoopGroup;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * @author XuHan
 * @date 2023/11/21 13:35
 */
@Slf4j
public class TestEventLoop {
    public static void main(String[] args) {
        //1。创建事件循环组
        // 如果不指定默认是2n核心数 DEFAULT_EVENT_LOOP_THREADS；指定了就以我指定的为主。指定两个线程数（2个事件对象）
        NioEventLoopGroup group = new NioEventLoopGroup(2);//IO事件 普通任务 定时任务
        //2。获取下一个事件循环对象
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        // EventLoop executors = group.next();
        //3.执行一个普通任务 提交给了事件循环组中某个对象去处理
        // 1.可以做异步处理 2.在做事件分发的时候
        group.next().submit(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
            log.debug("ok");
        });

        log.debug("main");
    }
}
