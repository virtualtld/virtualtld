package com.virtualtld.server;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class DnsServerTest {

    @Test
    public void test_echo() throws Exception {
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 5354);
        DnsServer server = new DnsServer(addr, req -> {
            try {
                req.socket.send(req.packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Executors.newSingleThreadExecutor().submit(server::start);
        DatagramSocket sock = new DatagramSocket();
        sock.send(new DatagramPacket(new byte[]{1, 2, 3, 4}, 4, new InetSocketAddress("127.0.0.1", 5354)));
        byte[] resp = new byte[4];
        sock.receive(new DatagramPacket(resp, 4));
        Assert.assertArrayEquals(new byte[]{1, 2, 3, 4}, resp);
        sock.close();
    }
}
