package com.virtualtld.client;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class RetryQueue {
    private final int[] schedule;
    private final PriorityQueue<Item> priorityQueue = new PriorityQueue<>();
    private final Map<Integer, Item> idMap = new HashMap<>();

    public RetryQueue(int[] schedule) {
        this.schedule = schedule;
    }

    private class Item implements Comparable<Item> {
        public final DnsRequest req;
        public final int alreadyRetriedTimes;
        public final long shouldRetryAt;
        public boolean removed = false;

        Item(DnsRequest req, int alreadyRetriedTimes, long sentAt) {
            this.req = req;
            this.alreadyRetriedTimes = alreadyRetriedTimes;
            int timeout = alreadyRetriedTimes < schedule.length
                    ? schedule[alreadyRetriedTimes]
                    : schedule[schedule.length - 1];
            shouldRetryAt = sentAt + timeout;
        }

        public Item retry() {
            return new Item(req, alreadyRetriedTimes + 1, shouldRetryAt);
        }

        @Override
        public int compareTo(Item that) {
            return Long.compare(shouldRetryAt, that.shouldRetryAt);
        }
    }

    public void add(DnsRequest req) {
        addItem(new Item(req, 0, req.createdAt));
    }

    public void remove(int reqId) {
        Item item = idMap.remove(reqId);
        if (item != null) {
            priorityQueue.remove(item);
        }
    }

    public DnsRequest retryNext(long now) {
        Item item = priorityQueue.peek();
        if (item == null) {
            return null;
        }
        if (now >= item.shouldRetryAt) {
            priorityQueue.remove(item);
            addItem(item.retry());
            return item.req;
        }
        return null;
    }

    private void addItem(Item item) {
        priorityQueue.add(item);
        idMap.put(item.req.getID(), item);
    }
}
