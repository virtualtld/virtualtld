package com.virtualtld.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DnsResender {

    private final static Logger LOGGER = LoggerFactory.getLogger(DnsResender.class);
    private final static ExecutorService executorService = Executors.newWorkStealingPool();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Consumer<DnsRequest> sendRequest;
    private final RetryQueue retryQueue;

    public DnsResender(Consumer<DnsRequest> sendRequest, int[] schedule) {
        this.sendRequest = sendRequest;
        this.retryQueue = new RetryQueue(schedule);
    }

    public void start() {
        executorService.submit(this::run);
    }

    public void stop() {
        running.set(false);
    }

    private void run() {
        while (running.get()) {
            try {
                runOnce();
            } catch (Exception e) {
                LOGGER.error("retrier failed", e);
            } finally {
                sleep100Millis();
            }
        }
    }

    private void sleep100Millis() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private synchronized void runOnce() throws Exception {
        while(true) {
            DnsRequest dnsRequest = retryQueue.retryNext(System.currentTimeMillis());
            if (dnsRequest == null) {
                return;
            }
            sendRequest.accept(dnsRequest);
        }
    }

    public synchronized void add(DnsRequest req) {
        retryQueue.add(req);
    }

    public synchronized void remove(int reqId) {
        retryQueue.remove(reqId);
    }
}
