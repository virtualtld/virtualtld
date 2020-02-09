package com.virtualtld.client;

import org.xbill.DNS.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Consumer;

public class ResilientDnsClient {
    private final Consumer<Message> respHandler;
    private final DnsClient dnsClient;
    private final Map<Integer, RetryRequest> requestMap = new HashMap<>();

    public ResilientDnsClient(Consumer<Message> respHandler) {
        this.respHandler = respHandler;
        dnsClient = newDnsClient(this::onResponse);
    }

    protected DnsClient newDnsClient(Consumer<Message> onResponse) {
        return new DnsClient(onResponse);
    }

    private synchronized void onResponse(Message resp) {
        respHandler.accept(resp);
    }

    public synchronized void send(Message req) {
        RetryRequest retryRequest = new RetryRequest(req, 0);
        requestMap.put(req.getHeader().getID(), retryRequest);
//        retryQueue.add(retryRequest);
        dnsClient.send(req, null);
    }

    public void start() {
        dnsClient.start();
    }

    public void stop() {
        dnsClient.stop();
    }

    private static class RetryRequest implements Comparable<RetryRequest> {
        public final Message req;
        public final int alreadyRetriedTimes;
        public final long shouldRetryAt;

        private RetryRequest(Message req, int alreadyRetriedTimes) {
            this.req = req;
            this.alreadyRetriedTimes = alreadyRetriedTimes;
            shouldRetryAt = System.currentTimeMillis() + 1000;
        }

        @Override
        public int compareTo(RetryRequest that) {
            return Long.compare(shouldRetryAt, that.shouldRetryAt);
        }
    }
}
