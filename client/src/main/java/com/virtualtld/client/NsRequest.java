package com.virtualtld.client;

import org.xbill.DNS.Message;

import java.net.URL;

public class NsRequest {

    private final URL url;

    public NsRequest(URL url) {
        this.url = url;
    }

    public Message nsRequest() {
        throw new RuntimeException();
    }
}

