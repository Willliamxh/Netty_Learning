package cn.itcast.NIO.c1;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author XuHan
 * @date 2023/11/16 14:43
 */
public class GettingWrites {
    public static void main(String[] args) {
        ByteBuffer buffer1 = StandardCharsets.UTF_8.encode("hello");
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("world");
        ByteBuffer buffer3 = StandardCharsets.UTF_8.encode("你好");
        //想在这边
        try (FileChannel channel = new RandomAccessFile("words2.txt", "rw").getChannel()) {
            channel.write(new ByteBuffer[]{buffer1,buffer2,buffer3});
        } catch (IOException e) {
        }

    //    这样的话不用一个一个在内核和用户态之间进行复制
    }
}
