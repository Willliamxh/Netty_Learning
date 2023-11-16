package cn.itcast.NIO.c1;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author XuHan
 * @date 2023/11/16 14:36
 * https://blog.csdn.net/u010429831/article/details/122452030?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-1-122452030-blog-133847609.235%5Ev38%5Epc_relevant_sort&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7ERate-1-122452030-blog-133847609.235%5Ev38%5Epc_relevant_sort&utm_relevant_index=1
 */
public class ScatteringReads {
    public static void main(String[] args) {
        try (FileChannel channel = new RandomAccessFile("3part.txt", "r").getChannel()) {
            ByteBuffer buffer1 = ByteBuffer.allocate(3);
            ByteBuffer buffer2 = ByteBuffer.allocate(3);
            ByteBuffer buffer3 = ByteBuffer.allocate(5);
            channel.read(new ByteBuffer[]{buffer1,buffer2,buffer3});
            buffer1.flip();
            buffer2.flip();
            buffer3.flip();
            ByteBufferUtil.debugAll(buffer1);
            ByteBufferUtil.debugAll(buffer2);
            ByteBufferUtil.debugAll(buffer3);

        } catch (IOException ioException) {
        };

    }
}
