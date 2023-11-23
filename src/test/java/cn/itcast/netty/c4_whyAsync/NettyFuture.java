package cn.itcast.netty.c4_whyAsync;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author XuHan
 * @date 2023/11/23 10:17
 */
@Slf4j
public class NettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("并行计算");
                Thread.sleep(1000);
                return 70;
            }
        });

        //假设我们的主线程想获得结果 同步方式获取结果
        // log.debug("等待结果");
        // log.debug("结果是：{}",future.get());
        // group.shutdownGracefully();

        // 异步方式获取结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("接收结果：{}",future.getNow());
            }
        });
        group.shutdownGracefully();


    }
}
