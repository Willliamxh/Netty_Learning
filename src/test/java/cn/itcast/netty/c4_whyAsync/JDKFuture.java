package cn.itcast.netty.c4_whyAsync;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author XuHan
 * @date 2023/11/23 10:05
 */
@Slf4j
public class JDKFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.线程池
        ExecutorService service = Executors.newFixedThreadPool(2);
        //2.提交任务
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 50;
            }
        });
        //3.主线程通过future和子线程通信
        log.debug("等待结果");
        Integer integer = future.get();//这边其实是个阻塞的
        log.debug("结果是:{}",integer);

        service.shutdown();

        while (!service.awaitTermination(1, TimeUnit.SECONDS)) {
            System.out.println("线程池没有关闭");
        }

        System.out.println("线程池已经关闭");


    }
}
