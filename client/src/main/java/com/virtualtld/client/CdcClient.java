package com.virtualtld.client;


import org.xbill.DNS.Message;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CdcClient {

    private final DnsClient dnsClient = new DnsClient(this::onResponse);
    private final static List<InetSocketAddress> ROOT_NAME_SERVERS = Arrays.asList(
            new InetSocketAddress("198.41.0.4", 53),
            new InetSocketAddress("199.9.14.201", 53),
            new InetSocketAddress("192.33.4.12", 53),
            new InetSocketAddress("199.7.91.13", 53),
            new InetSocketAddress("192.203.230.10", 53),
            new InetSocketAddress("192.5.5.241", 53),
            new InetSocketAddress("192.112.36.4", 53),
            new InetSocketAddress("198.97.190.53", 53),
            new InetSocketAddress("192.36.148.17", 53),
            new InetSocketAddress("192.58.128.30", 53),
            new InetSocketAddress("193.0.14.129", 53),
            new InetSocketAddress("199.7.83.42", 53),
            new InetSocketAddress("202.12.27.33", 53)
    );

    public void start() {
        dnsClient.start();
    }

    public void stop() {
        dnsClient.stop();
    }

    private synchronized void onResponse(Message resp) {
    }

    public synchronized void download(URI uri, Consumer<byte[]> callback) {
    }


}
