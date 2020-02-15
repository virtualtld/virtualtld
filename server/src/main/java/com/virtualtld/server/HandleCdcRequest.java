package com.virtualtld.server;

import com.protocol.cdc.Block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.net.DatagramPacket;
import java.util.Map;
import java.util.function.Consumer;

public class HandleCdcRequest implements Consumer<DnsRequest> {
    private final static Logger LOGGER = LoggerFactory.getLogger(HandleCdcRequest.class);
    private final Message nsResp;
    private final Map<String, Block> blocks;

    public HandleCdcRequest(Message nsResp, Map<String, Block> blocks) {
        this.nsResp = nsResp;
        this.blocks = blocks;
    }

    @Override
    public void accept(DnsRequest req) {
        try {
            this.handle(req);
        } catch (Exception e) {
            LOGGER.error("handle dns request failed", e);
        }
    }

    private void handle(DnsRequest req) throws Exception {
        Message input = new Message(req.packet.getData());
        LOGGER.info("input from " + req.packet.getAddress() + "\n" + input);
        Message output = new CdcResponse(input, nsResp, blocks).dnsResponse();
        byte[] outputBytes = output.toWire();
        LOGGER.info("send output of size " + outputBytes.length + "\n" + output);
        req.socket.send(new DatagramPacket(outputBytes, outputBytes.length, req.packet.getSocketAddress()));
    }
}
