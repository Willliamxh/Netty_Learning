package itcast.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import itcast.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author XuHan
 * @date 2023/11/28 19:22
 * * 必须和  LengthFieldBasedFrameDecoder frameDecoder = new LengthFieldBasedFrameDecoder(
 *  *                 // 最大长度  长度字段偏移量   长度本身字节          长度是否需要调整   是否保留后面字段
 *  *                 1024, 12, 4, 0, 0);
 *  * 一起使用，确保街道的byteBuf是完整的。不会出现线程安全问题
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodecShareable extends MessageToMessageCodec<ByteBuf, Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 1. 4 字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 2. 1 字节的版本,
        out.writeByte(1);
        // 3. 1 字节的序列化方式 jdk 0 , json 1
        out.writeByte(0);
        // 4. 1 字节的指令类型 在这个类里面有做了设计
        out.writeByte(msg.getMessageType());
        // 5. 4 个字节 通过@data或得 请求序号 双工通信使用
        out.writeInt(msg.getSequenceId());
        // 无意义，对齐填充 如果不加这个 就变成15个字节了 为了对象头对齐填充
        out.writeByte(0xff);
        // 6. 获取内容的字节数组
        // ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // // 把对象转化成二进制数组
        // ObjectOutputStream oos = new ObjectOutputStream(bos);
        // oos.writeObject(msg);
        // byte[] bytes = bos.toByteArray();
        // 用重新写的Serializer来序列化
        byte[] bytes = Serializer.Algorithm.Java.serialize(msg);
        // 7. 长度
        out.writeInt(bytes.length);
        // 8. 写入内容
        out.writeBytes(bytes);
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        // ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        // Message message = (Message) ois.readObject();
        // 把java序列化方法抽象到一个类中
        Message message = Serializer.Algorithm.Java.deserialize(Message.class, bytes);
        log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);
        log.debug("{}", message);
        out.add(message);
    }
}
