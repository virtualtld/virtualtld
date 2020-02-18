package com.virtualtld.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;
import org.xbill.DNS.Rcode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class DnsClient {

    private final static ExecutorService executorService = Executors.newWorkStealingPool();
    private final static Logger LOGGER = LoggerFactory.getLogger("vtld.DnsClient");
    private final Function<Message, String> respHandler;
    private final DnsResender resender = new DnsResender(
            this::doSend, new int[]{100, 200, 400, 800, 1600});
    private DatagramSocket sock;

    public DnsClient(Function<Message, String> respHandler) {
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

    private void run() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
        while (true) {
            sock.receive(packet);
            onResponse(packet.getData(), packet.getAddress(), packet.getPort());
        }
    }

    private synchronized void onResponse(byte[] respBytes, InetAddress remoteIp, int remotePort) {
        Message resp = null;
        try {
            resp = new Message(respBytes);
        } catch (Exception e) {
            LOGGER.error("failed to parse response from " + remoteIp + ":" + remotePort, e);
            return;
        }
        String log = "received dns response " + resp.getQuestion().getName() + " from " + remoteIp + ":" + remotePort + " with id " + resp.getHeader().getID();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(log + "\n" + resp);
        } else {
            LOGGER.info(log);
        }
        String result = handleResp(resp);
        LOGGER.info("handled dns response " + resp.getQuestion().getName() + " with result " + result);
    }

    private String handleResp(Message resp) {
        if (resp.getRcode() != Rcode.NOERROR) {
            LOGGER.warn(resp.getQuestion().getName() + " response rcode is not NOERROR: " + Rcode.TSIGstring(resp.getRcode()));
            return "rcode error";
        }
        try {
            String result = respHandler.apply(resp);
            resender.remove(resp.getHeader().getID());
            return result;
        } catch (Exception e) {
            LOGGER.warn("failed to handle response: \n" + resp, e);
            return "exception";
        }
    }

    private void doSend(DnsRequest dnsRequest) {
        if (dnsRequest.dropped) {
            LOGGER.info("retried too many times, dropped: " + dnsRequest.message);
            return;
        }
        byte[] reqBytes = dnsRequest.message.toWire();
        for (InetSocketAddress addr : pickThree(dnsRequest.candidateServers)) {
            try {
                sock.send(new DatagramPacket(reqBytes, reqBytes.length, addr));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void send(DnsRequest dnsRequest) {
        String log = "sent request + " + dnsRequest.message.getQuestion().getName() + " with id " + dnsRequest.getID();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(log + "\n" + dnsRequest.message);
        } else {
            LOGGER.info(log);
        }
        resender.add(dnsRequest);
        doSend(dnsRequest);
    }

    private static List<InetSocketAddress> pickThree(List<InetSocketAddress> candidateServers) {
        Random rand = ThreadLocalRandom.current();
        List<InetSocketAddress> list = new ArrayList<>(candidateServers);
        List<InetSocketAddress> picked = new ArrayList<>();
        for (int i = 0; i < Math.min(3, candidateServers.size()); i++) {
            int randomIndex = rand.nextInt(list.size());
            picked.add(list.get(randomIndex));
            list.remove(randomIndex);
        }
        return picked;
    }
}
