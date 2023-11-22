package cn.itcast.netty.channel;

/**
 * @author XuHan
 * @date 2023/11/22 16:33
 */
public class Test {
    public static void main(String[] args) {
        new Thread(()->{
            for (int i = 0; i < 100; i++) {
                System.out.println("jjj");
            }
        }).start();
        System.out.println("哈哈哈哈哈哈哈");
    }
}
