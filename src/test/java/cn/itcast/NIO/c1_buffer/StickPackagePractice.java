package cn.itcast.NIO.c1_buffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author XuHan
 * @date 2023/11/16 15:43
 * 粘包 半包现象以及处理方法

但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为

 * Hello,world\n
 * I'm zhangsan\n
 * How are you?\n

变成了下面的两个 byteBuffer (黏包，半包)

 * Hello,world\nI'm zhangsan\nHo （粘包，比如我想集中写，尽量地多的数据一次性发过来）
 * w are you?\n                    （半包，比如我缓冲区就这么大一次性只能发这么多）
 */
public class StickPackagePractice {
    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);

        source.put("w are you?\nhaha!\n".getBytes());
        split(source);

        // System.out.println((int)'\n');
        // System.out.println((char) 10);

    }
    private static void split(ByteBuffer source) {
        //切换到读模式
        source.flip();
        //遍历直到找到分隔符
        for (int i = 0; i < source.limit(); i++) {
            if(source.get(i)=='\n'){
                //把这条信息存入到一个新的buffer
                //先计算这个消息的长度 i+1-起始索引
                int len = i+1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(len);
                // 写入target
                for (int j = 0; j < len; j++) {
                    target.put(source.get());
                }
                target.flip();
                System.out.println(StandardCharsets.UTF_8.decode(target).toString());
                // System.out.println(Arrays.toString(StandardCharsets.UTF_8.decode(target).toString().getBytes(StandardCharsets.UTF_8)));
                // 这个工具类会把\n替换为.
                ByteBufferUtil.debugAll(target);
            }
        }

        //切换回写模式
        source.compact();
    }
}
