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

public class WebFilesTest {

    @Test
    public void scan_all() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/b/c/Hello.txt"), "hello".getBytes());
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        WebFiles webFiles = new WebFiles(fs.getPath("/a"), new WebFiles.Options());
        assertThat(webFiles.files(), equalTo(filesMap(fs,
                "b/c/Hello.txt", "/a/b/c/Hello.txt",
                "World.txt", "/a/World.txt")));
    }

    @Test
    public void dir_blacklist() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/b/c/Hello.txt"), "hello".getBytes());
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        WebFiles webFiles = new WebFiles(fs.getPath("/a"), new WebFiles.Options() {{
            directoryBlacklist.add(fs.getPathMatcher("glob:**/b"));
        }});
        assertThat(webFiles.files(), equalTo(filesMap(fs,
                "World.txt", "/a/World.txt")));
    }

    @Test
    public void file_whitelist() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/b/c/Hello.txt"), "hello".getBytes());
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        WebFiles webFiles = new WebFiles(fs.getPath("/a"), new WebFiles.Options() {{
            fileWhitelist.add(fs.getPathMatcher("glob:**/World.txt"));
        }});
        assertThat(webFiles.files(), equalTo(filesMap(fs,
                "World.txt", "/a/World.txt")));
    }

    @Test
    public void file_blacklist() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/b/c/Hello.txt"), "hello".getBytes());
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        WebFiles webFiles = new WebFiles(fs.getPath("/a"), new WebFiles.Options() {{
            fileBlacklist.add(fs.getPathMatcher("glob:**/World.txt"));
        }});
        assertThat(webFiles.files(), equalTo(filesMap(fs,
                "b/c/Hello.txt", "/a/b/c/Hello.txt")));
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
