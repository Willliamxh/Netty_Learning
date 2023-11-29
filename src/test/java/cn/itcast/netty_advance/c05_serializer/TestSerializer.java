package cn.itcast.netty_advance.c05_serializer;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;
import itcast.config.Config;
import itcast.message.LoginRequestMessage;
import itcast.message.Message;
import itcast.protocol.MessageCodecShareable;
import itcast.protocol.Serializer;

public class TestSerializer {

    public static void main(String[] args)  {
        MessageCodecShareable CODEC = new MessageCodecShareable();
        LoggingHandler LOGGING = new LoggingHandler();
        // 注意这边有两个LOGGING Handler
        EmbeddedChannel channel = new EmbeddedChannel(LOGGING, CODEC, LOGGING);

        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        // channel.writeOutbound(message);
        ByteBuf buf = messageToByteBuf(message);
        channel.writeInbound(buf);
    }

    public static ByteBuf messageToByteBuf(Message msg) {
        int algorithm = Config.getSerializerAlgorithm().ordinal();
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        out.writeBytes(new byte[]{1, 2, 3, 4});
        out.writeByte(1);
        out.writeByte(algorithm);
        out.writeByte(msg.getMessageType());
        out.writeInt(msg.getSequenceId());
        out.writeByte(0xff);
        byte[] bytes = Serializer.Algorithm.values()[algorithm].serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
        return out;
    }
}
