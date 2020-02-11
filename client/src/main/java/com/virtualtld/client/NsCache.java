package com.virtualtld.client;

import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NsCache {

    private final Consumer<DnsRequest> sendRequest;
    private final Consumer<Message> onResponse;
    private final Map<Name, Message> cache = new HashMap<>();

    public NsCache(Consumer<DnsRequest> sendRequest, Consumer<Message> onResponse) {
        this.sendRequest = sendRequest;
        this.onResponse = onResponse;
    }

    public synchronized void sendRequest(DnsRequest req) {
        Record question = req.message.getQuestion();
        if (question.getType() == Type.NS){
            Message resp = cache.get(question.getName());
            if (resp != null) {
                Message clonedResp = (Message) resp.clone();
                clonedResp.getHeader().setID(req.getID());
                this.onResponse(clonedResp);
                return;
            }
        }
        this.sendRequest.accept(req);
    }

    public synchronized void onResponse(Message resp) {
        this.onResponse.accept(resp);
        Record question = resp.getQuestion();
        if (question.getType() == Type.NS) {
            cache.put(question.getName(), resp);
        }
    }
}
