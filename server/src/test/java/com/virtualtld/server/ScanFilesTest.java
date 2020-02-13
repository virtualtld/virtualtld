package com.virtualtld.server;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ScanFilesTest {

    @Test
    public void index_html() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/index.html"), "hello".getBytes());
        ScanFiles scanFiles = new ScanFiles(fs.getPath("/a"), new ScanOptions());
        assertThat(scanFiles.files(), equalTo(filesMap(fs,
                "/index.html", "/a/index.html",
                "/", "/a/index.html")));
    }

    @Test
    public void scan_all() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/b/c/Hello.txt"), "hello".getBytes());
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        ScanFiles scanFiles = new ScanFiles(fs.getPath("/a"), new ScanOptions());
        assertThat(scanFiles.files(), equalTo(filesMap(fs,
                "/b/c/Hello.txt", "/a/b/c/Hello.txt",
                "/World.txt", "/a/World.txt")));
    }

    @Test
    public void dir_blacklist() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/b/c/Hello.txt"), "hello".getBytes());
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        ScanFiles scanFiles = new ScanFiles(fs.getPath("/a"), new ScanOptions() {{
            directoryBlacklist.add(fs.getPathMatcher("glob:**/b"));
        }});
        assertThat(scanFiles.files(), equalTo(filesMap(fs,
                "/World.txt", "/a/World.txt")));
    }

    @Test
    public void file_whitelist() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/b/c/Hello.txt"), "hello".getBytes());
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        ScanFiles scanFiles = new ScanFiles(fs.getPath("/a"), new ScanOptions() {{
            fileWhitelist.add(fs.getPathMatcher("glob:**/World.txt"));
        }});
        assertThat(scanFiles.files(), equalTo(filesMap(fs,
                "/World.txt", "/a/World.txt")));
    }

    @Test
    public void file_blacklist() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/b/c/Hello.txt"), "hello".getBytes());
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        ScanFiles scanFiles = new ScanFiles(fs.getPath("/a"), new ScanOptions() {{
            fileBlacklist.add(fs.getPathMatcher("glob:**/World.txt"));
        }});
        assertThat(scanFiles.files(), equalTo(filesMap(fs,
                "/b/c/Hello.txt", "/a/b/c/Hello.txt")));
    }

    private HashMap<String, Path> filesMap(FileSystem fs, String... args) {
        return new HashMap<String, Path>() {{
            for (int i = 0; i < args.length; i += 2) {
                String relPath = args[i];
                String absPath = args[i + 1];
                put(relPath, fs.getPath(absPath));
            }
        }};
    }
}
