package com.virtualtld.server;

import com.protocol.cdc.Block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.net.DatagramPacket;
import java.util.Map;
import java.util.function.Consumer;

public class CdcRequestHandler implements Consumer<DnsRequest> {
    private final static Logger logger = LoggerFactory.getLogger(CdcRequestHandler.class);
    private final Message nsResp;
    private final Map<String, Block> blocks;

    public CdcRequestHandler(Message nsResp, Map<String, Block> blocks) {
        this.nsResp = nsResp;
        this.blocks = blocks;
    }

    @Override
    public void accept(DnsRequest req) {
        try {
            this.handle(req);
        } catch (Exception e) {
            logger.error("handle dns request failed", e);
        }
    }

    private void handle(DnsRequest req) throws Exception {
        Message input = new Message(req.packet.getData());
        logger.info("input\n" + input);
        Message output = new CdcResponse(input, nsResp, blocks).dnsResponse();
        logger.info("output\n" + output);
        byte[] outputBytes = output.toWire();
        req.socket.send(new DatagramPacket(outputBytes, outputBytes.length, req.packet.getSocketAddress()));
    }
}
