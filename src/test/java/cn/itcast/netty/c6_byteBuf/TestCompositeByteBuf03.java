package cn.itcast.netty.c6_byteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

/**
 * @author XuHan
 * @date 2023/11/27 11:27
 */
public class TestCompositeByteBuf03 {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{1,2,3,4,5});

        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{6,7,8,9,10});

        // 多次拷贝 进行组合
        // ByteBuf buf3 = ByteBufAllocator.DEFAULT.buffer(10);
        // buf3.writeBytes(buf).writeBytes(buf2);
        // BufLogUtils.log(buf3);

        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        compositeByteBuf.addComponents(true,buf,buf2);
        BufLogUtils.log(compositeByteBuf);


    }
}
