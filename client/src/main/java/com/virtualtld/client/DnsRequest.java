package com.virtualtld.client;

import org.xbill.DNS.Message;

import java.net.InetSocketAddress;
import java.util.List;

public class DnsRequest {

    public long createdAt = System.currentTimeMillis();
    public final Message message;
    public final List<InetSocketAddress> candidateServers;
    public boolean dropped = false;

    public DnsRequest(Message message, List<InetSocketAddress> candidateServers) {
        this.message = message;
        this.candidateServers = candidateServers;
    }

    public int getID() {
        return message.getHeader().getID();
    }
}
