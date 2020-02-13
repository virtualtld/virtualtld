package com.virtualtld.server;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.protocol.cdc.Block;
import com.protocol.cdc.VirtualtldSite;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.Assert.assertThat;

public class EncodedDirectoryTest {

    @Test
    public void one_file() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        Map<String, Block> blocks = newEncodedDirectory(fs.getPath("/a")).blocks();
        assertThat(blocks, aMapWithSize(3));
    }

    @Test
    public void two_files() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectories(fs.getPath("/a/b/c"));
        Files.write(fs.getPath("/a/b/c/Hello.txt"), "hello".getBytes());
        Files.write(fs.getPath("/a/World.txt"), "world".getBytes());
        Map<String, Block> blocks = newEncodedDirectory(fs.getPath("/a")).blocks();
        assertThat(blocks, aMapWithSize(6));
    }

    private static EncodedDirectory newEncodedDirectory(Path webRoot) {
        VirtualtldSite site = new VirtualtldSite("最新版本.com", "最新版本.xyz");
        return new EncodedDirectory(site, webRoot, new ScanOptions());
    }
}
