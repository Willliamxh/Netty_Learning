package cn.itcast.NIO.c6_Aio;

import cn.itcast.NIO.c1_buffer.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author XuHan
 * @date 2023/11/20 19:43
 */
@Slf4j
public class AioDemo {
    public static void main(String[] args) throws Exception {
        try (AsynchronousFileChannel s =
                     AsynchronousFileChannel.open(
                             Paths.get("data.txt"), StandardOpenOption.READ);) {
            //bytebuffer 读取的起始位置 附件 回调函数
            ByteBuffer buffer = ByteBuffer.allocate(16);
            log.debug("read begin...");
            s.read(buffer, 0, null, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    // 文件正常读取完毕后
                    log.debug("read success...{}",result);
                    buffer.flip();
                    ByteBufferUtil.debugAll(buffer);
                    try {
                        s.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    // read出现异常了
                    log.debug("read failed...");
                    exc.printStackTrace();
                }
            });
            log.debug("read end...");
            // 这边因为用了try-with-resource 会直接把我们的资管关闭，没读完就关闭了 会有异常java.nio.channels.AsynchronousCloseException
            System.in.read();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.in.read();
    }
}
