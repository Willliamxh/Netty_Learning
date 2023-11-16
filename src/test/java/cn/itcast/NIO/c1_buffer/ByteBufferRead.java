package cn.itcast.NIO.c1_buffer;

import java.nio.ByteBuffer;

/**
 * @author XuHan
 * @date 2023/11/16 13:55
 */
public class ByteBufferRead {
    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byteBuffer.put(new byte[]{'A','b','c','d'});
        // ByteBufferUtil.debugAll(byteBuffer);

        // byteBuffer.flip();
        // byteBuffer.get(new byte[4]);
        // ByteBufferUtil.debugAll(byteBuffer);
        //
        // byteBuffer.rewind();
        // ByteBufferUtil.debugAll(byteBuffer);

    //    mark @ reset
    //    mark 记录一个位置
    //    reset 将position重置到mark的位置
    //     byteBuffer.flip();
    //     System.out.println(byteBuffer.get());
    //     System.out.println(byteBuffer.get());
    //     // 标记存档2的位置
    //     byteBuffer.mark();
    //     System.out.println(byteBuffer.get());
    //     System.out.println(byteBuffer.get());
    //     //将position重置为2的位置
    //     byteBuffer.reset();
    //     System.out.println(byteBuffer.get());

        //  get(i) 不影响position位置
        System.out.println(byteBuffer.get(3));
        ByteBufferUtil.debugAll(byteBuffer);


    }
}
