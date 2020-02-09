package com.virtualtld.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DnsClient {

    private final static ExecutorService executorService = Executors.newWorkStealingPool();
    private final static Logger LOGGER = LoggerFactory.getLogger(DnsClient.class);
    private final Consumer<Message> respHandler;
    private DatagramSocket sock;

    public DnsClient(Consumer<Message> respHandler) {
        this.respHandler = respHandler;
        try {
            sock = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        executorService.submit(() -> {
            try {
                run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void run() throws Exception {
        DatagramPacket packet = new DatagramPacket(new byte[512], 512);
        while (true) {
            sock.receive(packet);
            Message resp = new Message(packet.getData());
            try {
                respHandler.accept(resp);
            } catch (Exception e) {
                LOGGER.debug("failed to handle response: \n" + resp, e);
            }
        }
    }

    public void stop() {
        if (sock != null) {
            sock.close();
        }
    }

    public void send(Message req, InetSocketAddress addr) {
        byte[] reqBytes = req.toWire();
        try {
            sock.send(new DatagramPacket(reqBytes, reqBytes.length, addr));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
