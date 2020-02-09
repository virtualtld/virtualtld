package com.virtualtld.client;


import org.xbill.DNS.Message;

import java.net.URI;
import java.util.function.Consumer;

public class CdcClient {

    private final ResilientDnsClient dnsClient = new ResilientDnsClient(this::onResponse);

    private synchronized void onResponse(Message resp) {

    }

    public synchronized void download(URI uri, Consumer<byte[]> callback) {
    }


}
