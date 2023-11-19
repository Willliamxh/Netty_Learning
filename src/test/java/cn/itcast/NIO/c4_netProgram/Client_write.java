package cn.itcast.NIO.c4_netProgram;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author XuHan
 * @date 2023/11/19 16:33
 */
public class Client_write {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel =SocketChannel.open();

        boolean connect = socketChannel.connect(new InetSocketAddress(8080));

        //3.接收数据
        int count = 0;
        while (true){
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            count += socketChannel.read(buffer);
            System.out.println(count);
            buffer.clear();
        }
    }
}
