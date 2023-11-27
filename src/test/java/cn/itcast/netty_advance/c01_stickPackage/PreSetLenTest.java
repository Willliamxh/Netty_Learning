package cn.itcast.netty_advance.c01_stickPackage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author XuHan
 * @date 2023/11/27 16:39
 * 预设长度
 */
public class PreSetLenTest {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                // 最大长度，长度偏移，长度占用字节，长度调整（长度之后还有几个字节开始），剥离字节数（从头开始跳过几个字节）
                new LengthFieldBasedFrameDecoder(1024,0,4,1,4),
                new LoggingHandler(LogLevel.DEBUG));

        //4个字节内容长度，实际内容】、
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer, "hello, world");
        send(buffer, "Hi!");
        channel.writeInbound(buffer);
    }

    private static void send(ByteBuf buffer, String content) {
        byte[] bytes = content.getBytes();
        // 实际内容长度
        int length = bytes.length;
        buffer.writeInt(length);
        // 加入了版本号 保留版本
        buffer.writeByte(1);
        buffer.writeBytes(bytes);
    }
}
