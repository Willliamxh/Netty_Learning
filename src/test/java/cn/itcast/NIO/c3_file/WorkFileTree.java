package cn.itcast.NIO.c3_file;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author XuHan
 * @date 2023/11/16 17:48
 */
public class WorkFileTree {
    public static void main(String[] args) throws IOException {
        String local = "/Users/xuhan/program/program/workLearning/Learn_netty/Netty-demo";

        m4();


    }

    /**
     * 拷贝多级目录
     * @throws IOException
     */
    private static void m4() throws IOException {
        String source = "D:\\Snipaste-1.16.2-x64";
        String target = "D:\\Snipaste-1.16.2-x64aaa";

        Files.walk(Paths.get(source)).forEach(path -> {
            try {
                String targetName = path.toString().replace(source, target);
                // 是目录
                if (Files.isDirectory(path)) {
                    Files.createDirectory(Paths.get(targetName));
                }
                // 是普通文件
                else if (Files.isRegularFile(path)) {
                    Files.copy(path, Paths.get(targetName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void m3(String local) throws IOException {
        // 删除多级目录
        Path path = Paths.get(local);
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    /**
     * 查找有多少个txt文件
     * @param local
     * @throws IOException
     */
    private static void m2(String local) throws IOException {
        // 查找有多少个txt文件
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(Paths.get(local),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().endsWith(".txt")) {
                    System.out.println("文件===="+file);
                    fileCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    /**
     * 查看文件数
     * @param local
     * @throws IOException
     */
    private static void m1(String local) throws IOException {
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(Paths.get(local),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("文件夹===="+dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("文件===="+file);
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });
        System.out.println(dirCount); // 133
        System.out.println(fileCount); // 1479
    }
}
