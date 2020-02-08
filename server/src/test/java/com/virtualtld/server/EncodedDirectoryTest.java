package com.virtualtld.server;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class EncodedDirectoryTest {

    @Test
    public void spike() throws IOException {
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
                "glob:**/*.zip");
        System.out.println(pathMatcher.matches(Paths.get("hello", "world", "a.zip")));;
        System.out.println(pathMatcher.matches(Paths.get("hello", "world", "a.txt")));;

        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/root"));
        Files.write(fs.getPath("/root/hello.txt"), "hello".getBytes());
        Files.walkFileTree(fs.getPath("/root"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
