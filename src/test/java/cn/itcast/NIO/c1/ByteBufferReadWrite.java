package cn.itcast.NIO.c1;

import java.nio.ByteBuffer;

/**
 * @author XuHan
 * @date 2023/11/16 13:24
 * 1.buffer写入数据方式：
 *      -调用 channel 的 read 方法  int readBytes = channel.read(buf);（channel之前可以读一个文件之类的）
 *      -调用 buffer 自己的 put 方法 buf.put((byte)127);
 * 2.buffer读取数据的方式：
 *          -调用 channel 的 write 方法（往channel里面写数据）
 *          -调用 buffer 自己的 get 方法
 */
public class ByteBufferReadWrite {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        // 放个a进去
        buffer.put((byte)0x61);
        ByteBufferUtil.debugAll(buffer);
        buffer.put(new byte[]{0x62,0x63,0x64});
        ByteBufferUtil.debugAll(buffer);
        //如果这个时候不切换模式，直接去读，会读不到东西
        //切换到读模式
        buffer.flip();
        //flip 动作发生后，position 切换为读取位置，limit 切换为读取限制
        System.out.println(buffer.get());
        ByteBufferUtil.debugAll(buffer);
        // 切换到写模式，把未读的数据往前压缩 这个时候多了个64，但是问题不大，因为之后再写会覆盖这个64
        buffer.compact();
        ByteBufferUtil.debugAll(buffer);
        buffer.put(new byte[]{0x65,0x66,0x67});
        ByteBufferUtil.debugAll(buffer);

    }
}
