package cn.itcast.NIO.c1_buffer;

import java.nio.ByteBuffer;

/**
 * @author XuHan
 * @date 2023/11/16 13:36
 */
public class ByteBufferAllocate {
    public static void main(String[] args) {
        System.out.println(ByteBuffer.allocate(10).getClass());
        System.out.println(ByteBuffer.allocateDirect(10).getClass());
        /**
         class java.nio.HeapByteBuffer      -java堆内存，读写效率较低。会收到垃圾回收的影响。
         class java.nio.DirectByteBuffer    -直接内存（底层操作系统用的MMAP机制），读写效率较高，会少一次数据的拷贝。
                                            -使用的是系统内存，不会收到GC影响。
                                            -分配效率较低。
         */
    }
}
