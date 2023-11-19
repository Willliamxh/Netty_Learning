package cn.itcast.NIO;

import cn.itcast.NIO.c1_buffer.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author XuHan
 * @date 2023/11/16 17:31
 */
public class Test {
    public static void main(String[] args) {
        //2^31-1 = 2*2^10*2^10*2^10 =2G
        // System.out.println(Integer.MAX_VALUE);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 2; i++) {
            sb.append("a");
        }
        ByteBuffer byteBuffer = Charset.defaultCharset().encode(sb.toString());
        // System.out.println();
        // 在position基础上往后写
        byteBuffer.put((byte)'b');
        byteBuffer.put((byte)'c');
        // System.out.println(byteBuffer);
        // System.out.println(Charset.defaultCharset().decode(byteBuffer).toString());

        byteBuffer.flip();
        // limit = position;
        // position = 0;
        // mark = -1;
        // 给老子回去读
        ByteBufferUtil.debugRead(byteBuffer);
    }
}
