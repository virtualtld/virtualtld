package com.virtualtld.server;

import com.virtualtld.client.CdcClient;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class IntegrationTest {

    @Test
    public void download_one_file() throws Exception {
        runTest("hello".getBytes());
    }

    @Test
    public void download_two_chunk() throws Exception {
        runTest("You should learn about bytes, numeral systems, characters and their encoding in bits, called 'character encoding'. By default, an ascii character is encoded using 1 byte (8 bits). Hexadecimal is a numeral system where each number can span from 0-F. This number can be represented by an ascii character. You need two hex numbers to describe a byte (8 bits). If you want to display these two numbers using ascii characters, then you need two characters and thus two bytes.".getBytes());
    }

    @Test
    public void download_large_file() throws Exception {
        byte[] largeContent = new byte[1024 * 1024];
        new Random().nextBytes(largeContent);
        runTest(largeContent);
    }

    private void runTest(byte[] content) throws InterruptedException {
        ExecutorService executorService = Executors.newWorkStealingPool();
        executorService.submit(() -> {
            try {
                Path webRoot = Paths.get("/tmp/webroot");
                Files.createDirectories(webRoot);
                Files.write(webRoot.resolve("virtualtld.conf"), ("ListenAt=127.0.0.1:8383\n" +
                        "Version=1.1\n" +
                        "PublicDomain=最新版本.com\n" +
                        "PrivateDomain=最新版本.xyz\n" +
                        "PrivateResolver=127.0.0.1:8383").getBytes());
                Files.write(webRoot.resolve("index.html"), content);
                Serve.serve(webRoot);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        CdcClient cdcClient = new CdcClient();
        cdcClient.rootNameServers = Collections.singletonList(
                new InetSocketAddress("127.0.0.1", 8383));
        cdcClient.start();
        Exchanger<byte[]> exchanger = new Exchanger<>();
        cdcClient.download(URI.create("virtualtld://最新版本.com/"), resp -> {
            try {
                exchanger.exchange(resp);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        assertThat(exchanger.exchange(null), equalTo(content));
    }
}
