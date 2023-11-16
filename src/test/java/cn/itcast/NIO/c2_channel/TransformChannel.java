package cn.itcast.NIO.c2_channel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * @author XuHan
 * @date 2023/11/16 17:04
 */
public class TransformChannel {
    public static void main(String[] args) {
        try (FileChannel fromChannel = new FileInputStream("data.txt").getChannel();
             FileChannel toChannel = new FileOutputStream("to.txt").getChannel();
        ) {
            //底层用的sendFile进行优化，
            // fromChannel.transferTo(0,fromChannel.size(),toChannel);
            // transferTo最多处理2G数据 也就是Integer.MAX_VALUE-1个数据
            //  如果处理超过2g的数据，需要分批处理
            long size = fromChannel.size();
            //left 代表还剩多少字节
            for (long left = size; left >0 ; ) {
                System.out.println("position:" + (size - left) + " left:" + left);
                left = left - fromChannel.transferTo(size-left, left,toChannel);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
