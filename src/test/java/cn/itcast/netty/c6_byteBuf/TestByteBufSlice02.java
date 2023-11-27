package cn.itcast.netty.c6_byteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * @author XuHan
 * @date 2023/11/27 11:09
 */
public class TestByteBufSlice02 {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});
        BufLogUtils.log(buf);
        //在切片的过程中 并没有发生数据复制
        ByteBuf buf1 = buf.slice(0, 5);
        buf1.retain();
        ByteBuf buf2 = buf.slice(5, 5);

        BufLogUtils.log(buf1);
        BufLogUtils.log(buf2);

        // buf1.setByte(0,'z');
        // BufLogUtils.log(buf);
        // BufLogUtils.log(buf1);
        // BufLogUtils.log(buf2);

        // 释放原有buf内存
        buf.release();
        // 切片后的buf1 buf2是同一块内存 原有的释放掉了，我这个也会被释放 怎么办呢？
        // 使用 buf1.retain(); 引用计数+1
        BufLogUtils.log(buf1);
        // 用完释放
        buf1.release();
        //





    }
}
