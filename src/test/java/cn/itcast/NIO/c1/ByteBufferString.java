package cn.itcast.NIO.c1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author XuHan
 * @date 2023/11/16 14:20
 */
public class ByteBufferString {
    public static void main(String[] args) {
        // 方法1.字符串转为byteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put("hello".getBytes());
        ByteBufferUtil.debugAll(buffer);

        // 方法2.charset将字符串转为byteBuffer 这边会自动切换为读模式
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");
        ByteBufferUtil.debugAll(buffer2);

        //3.warp方法 也是自动切换为读模式
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
        ByteBufferUtil.debugAll(buffer3);


        //将buffer转化为String 这边2和3直接转化没问题 但是1的话得先切换为读模式再去转化
        String string = StandardCharsets.UTF_8.decode(buffer2).toString();
        System.out.println(string);

        //必须要切换为读模式
        buffer.flip();
        String s = StandardCharsets.UTF_8.decode(buffer).toString();
        System.out.println(s);
    }
}
