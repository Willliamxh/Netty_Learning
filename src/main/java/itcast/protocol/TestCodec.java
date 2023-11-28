package itcast.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import itcast.message.LoginRequestMessage;

/**
 * @author XuHan
 * @date 2023/11/28 14:12
 */
public class TestCodec {
    public static void main(String[] args) throws Exception {
        // 工序
        // 工人1 1234
        // 工人2 1234 会把工人1和工人2的给组成一起了 所以尽量不要抽成变量给多个Channel用 会有线程安全问题
        LengthFieldBasedFrameDecoder frameDecoder = new LengthFieldBasedFrameDecoder(
                // 最大长度  长度字段偏移量   长度本身字节          长度是否需要调整   是否保留后面字段
                1024, 12, 4, 0, 0);
        // 工序
        // log没有线程安全问题 @Sharable 可以被共享
        LoggingHandler loggingHandler = new LoggingHandler();

        EmbeddedChannel channel = new EmbeddedChannel(loggingHandler,
                // 为了解决粘包半包问题
                frameDecoder,
                new MessageCodec());

        // 测试encode
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        // 写入会调用编码器
        channel.writeOutbound(message);

        //测试decode
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,buf);
        // channel.writeInbound(buf);


        ByteBuf s1 = buf.slice(0, 100);
        ByteBuf s2 = buf.slice(100, buf.readableBytes() - 100);
        s1.retain(); // 引用计数+1 = 2
        channel.writeInbound(s1); // 这边写完之后会调用 release1 slice是林拷贝 如果释放掉了 s2也没了
        channel.writeInbound(s2);
    }

}
