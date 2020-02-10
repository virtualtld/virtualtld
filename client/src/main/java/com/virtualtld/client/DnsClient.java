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
    private final DnsResender resender = new DnsResender(
            this::doSend, new int[]{100, 200, 400, 800, 1600});
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
        resender.start();
        executorService.submit(() -> {
            try {
                run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void stop() {
        resender.stop();
        if (sock != null) {
            sock.close();
        }
    }

    private void run() throws Exception {
        DatagramPacket packet = new DatagramPacket(new byte[512], 512);
        while (true) {
            sock.receive(packet);
            Message resp = new Message(packet.getData());
            try {
                respHandler.accept(resp);
                resender.remove(resp.getHeader().getID());
            } catch (Exception e) {
                LOGGER.debug("failed to handle response: \n" + resp, e);
            }
        }
    }

    private void doSend(DnsRequest dnsRequest) {
        if (dnsRequest.dropped) {
            return;
        }
        byte[] reqBytes = dnsRequest.message.toWire();
        for (InetSocketAddress addr : dnsRequest.candidateServers) {
            try {
                sock.send(new DatagramPacket(reqBytes, reqBytes.length, addr));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void send(DnsRequest dnsRequest) {
        resender.add(dnsRequest);
        doSend(dnsRequest);
    }
}
