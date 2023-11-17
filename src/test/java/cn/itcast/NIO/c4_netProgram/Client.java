package cn.itcast.NIO.c4_netProgram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author XuHan
 * @date 2023/11/17 13:25
 * 这边运行的时候，debug运行
 * 对Socketchannel写Evaluate。
 *  * socketChannel.write(Charset.defaultCharset().encode("hi2333"));
 *  可以发现
 * 单线程阻塞模式的缺点：所有的方法，某个方法执行时，会影响其他方法
 * 你accept的时候就不能read了，read的时候就不能Accept了。
 * 除非我给每个进来的连接都单独创建一个线程。
 */
public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(8080));
        System.out.println("waiting");
    }
}
