package com.virtualtld.client;

import org.junit.Test;
import org.xbill.DNS.Message;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RetryQueueTest {

    @Test
    public void timeout_not_reached_yet() {
        RetryQueue retryQueue = new RetryQueue(new int[]{1});
        DnsRequest req = new DnsRequest(new Message(), new ArrayList<>());
        req.createdAt = 100;
        retryQueue.add(req);
        assertThat(retryQueue.retryNext(100), equalTo(null));
    }

    @Test
    public void timeout_reached() {
        RetryQueue retryQueue = new RetryQueue(new int[]{1});
        DnsRequest req = new DnsRequest(new Message(), new ArrayList<>()) {{
            createdAt = 100;
        }};
        retryQueue.add(req);
        assertThat(retryQueue.retryNext(101), equalTo(req));
        assertThat(retryQueue.retryNext(101), equalTo(null));
    }

    @Test
    public void retried_then_timeout() {
        RetryQueue retryQueue = new RetryQueue(new int[]{1, 2});
        DnsRequest req = new DnsRequest(new Message(), new ArrayList<>()) {{
            createdAt = 100;
        }};
        retryQueue.add(req);
        assertThat(retryQueue.retryNext(101), equalTo(req));
        assertThat(retryQueue.retryNext(102), equalTo(null));
        assertThat(retryQueue.retryNext(103), equalTo(req));
    }

    @Test
    public void multiple_timed_out_items() {
        RetryQueue retryQueue = new RetryQueue(new int[]{1, 5});
        DnsRequest req1 = new DnsRequest(new Message(), new ArrayList<>()) {{
            createdAt = 101;
        }};
        retryQueue.add(req1);
        DnsRequest req2 = new DnsRequest(new Message(), new ArrayList<>()) {{
            createdAt = 100;
        }};
        retryQueue.add(req2);
        assertThat(retryQueue.retryNext(102), equalTo(req2));
        assertThat(retryQueue.retryNext(102), equalTo(req1));
        assertThat(retryQueue.retryNext(102), equalTo(null));
    }

    @Test
    public void removed_item() {
        RetryQueue retryQueue = new RetryQueue(new int[]{1, 5});
        DnsRequest req1 = new DnsRequest(new Message(), new ArrayList<>()) {{
            createdAt = 101;
        }};
        retryQueue.add(req1);
        DnsRequest req2 = new DnsRequest(new Message(), new ArrayList<>()) {{
            createdAt = 100;
        }};
        retryQueue.add(req2);
        retryQueue.remove(req2.getID());
        assertThat(retryQueue.retryNext(102), equalTo(req1));
        assertThat(retryQueue.retryNext(102), equalTo(null));
    }
}
