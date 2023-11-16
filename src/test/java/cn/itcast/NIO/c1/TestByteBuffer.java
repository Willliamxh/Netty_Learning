package cn.itcast.NIO.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author XuHan
 * @date 2023/11/16 11:03
 */
@Slf4j
public class TestByteBuffer {
    public static void main(String[] args) {
        // fileChannel
        // 1.输入输出流 2.RandomAccessFile
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            // 准备缓冲区 10 一次性只能读10个数据
            ByteBuffer buffer = ByteBuffer.allocate(10);

            while (true){
                // 从channel读取数据，写入buffer
                int len = channel.read(buffer);
                log.debug("读取的字节数:{}",len);
                if(len==-1){
                    break;
                }
                // 打印buffer内容
                // 先切换至读模式
                buffer.flip();
                while(buffer.hasRemaining()) {
                    byte b = buffer.get();
                    log.debug("实际字节:{}",(char)b);
                }
            //    切换为写模式
                buffer.clear();
            }
        }catch (IOException e){

        }

    }
}
