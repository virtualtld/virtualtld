package com.virtualtld.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class RetryWatcher {

    private final static Logger LOGGER = LoggerFactory.getLogger(RetryWatcher.class);
    private final static ExecutorService executorService = Executors.newWorkStealingPool();
    private final PriorityQueue<Item> queue = new PriorityQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final BiConsumer<Message, InetSocketAddress> sendMessage;
    private final int[] schedule;

    public RetryWatcher(BiConsumer<Message, InetSocketAddress> sendMessage, int[] schedule) {
        this.sendMessage = sendMessage;
        this.schedule = schedule;
    }

    public void start() {
        executorService.submit(this::run);
    }

    public void stop() {
        running.set(false);
    }

    private void run() {
        while(running.get()) {
            try {
                runOnce();
            } catch (Exception e) {
                LOGGER.error("retrier failed", e);
            } finally {
                sleepOneSecond();
            }
        }
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private synchronized void runOnce() throws Exception {
    }

    public synchronized void addItem(Item item) {
    }

    public synchronized void removeItem(int reqId) {
    }

    static class Item implements Comparable<Item> {
        public final Message req;
        public final List<InetSocketAddress> candidateServers;
        public final int alreadyRetriedTimes;
        public final long shouldRetryAt;
        public boolean removed = false;

        Item(Message req, List<InetSocketAddress> candidateServers, int alreadyRetriedTimes) {
            this.req = req;
            this.candidateServers = candidateServers;
            this.alreadyRetriedTimes = alreadyRetriedTimes;
            shouldRetryAt = System.currentTimeMillis() + 1000;
        }

        @Override
        public int compareTo(Item that) {
            return Long.compare(shouldRetryAt, that.shouldRetryAt);
        }
    }
}
