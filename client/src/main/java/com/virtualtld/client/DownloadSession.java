package com.virtualtld.client;

import org.xbill.DNS.Message;

import java.util.function.Consumer;

public class DownloadSession {

    private final Message nsReq;
    private final Consumer<Message> sendRequest;

    public DownloadSession(Message nsReq, Consumer<Message> sendRequest) {
        this.nsReq = nsReq;
        this.sendRequest = sendRequest;
        sendRequest.accept(nsReq);
    }

    public void onResponse(Message resp) {
    }

    public byte[] result() {
        throw new RuntimeException();
    }
}
