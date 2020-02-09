package com.virtualtld.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HandleConcurrently implements Consumer<DnsRequest> {

    private final Consumer<DnsRequest> handler;
    private final ExecutorService executorService;

    public HandleConcurrently(Consumer<DnsRequest> handler, int nThreads) {
        this.handler = handler;
        executorService = Executors.newFixedThreadPool(nThreads);
    }

    @Override
    public void accept(DnsRequest dnsRequest) {
        executorService.submit(() -> {
            handler.accept(dnsRequest);
        });
    }
}
