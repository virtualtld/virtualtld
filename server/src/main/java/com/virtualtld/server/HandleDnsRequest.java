package com.virtualtld.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.net.DatagramPacket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class HandleDnsRequest implements Consumer<DnsRequest> {
    private final static Logger logger = LoggerFactory.getLogger(HandleDnsRequest.class);
    private final ExecutorService threadPool;
    private final Function<Message, Message> handleQuery;

    public HandleDnsRequest(Function<Message, Message> handleQuery) {
        this.handleQuery = handleQuery;
        threadPool = Executors.newFixedThreadPool(8);
    }

    @Override
    public void accept(DnsRequest req) {
        this.threadPool.submit(() -> {
            try {
                this.acceptInThread(req);
            } catch (Exception e) {
                logger.error("handle dns request failed", e);
            }
        });
    }

    private void acceptInThread(DnsRequest req) throws Exception {
        Message input = new Message(req.packet.getData());
        logger.info("input\n" + input);
        Message output = handleQuery.apply(input);
        logger.info("output\n" + output);
        byte[] outputBytes = output.toWire();
        req.socket.send(new DatagramPacket(outputBytes, outputBytes.length, req.packet.getSocketAddress()));
    }
}
