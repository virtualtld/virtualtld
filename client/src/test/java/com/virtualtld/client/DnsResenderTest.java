package com.virtualtld.client;

import org.junit.Test;
import org.xbill.DNS.Message;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DnsResenderTest {

    @Test
    public void resend_after_timeout() throws Exception {
        AtomicBoolean retried = new AtomicBoolean(false);
        DnsResender dnsResender = new DnsResender((req) -> {
            retried.set(true);
        }, new int[]{1});
        dnsResender.add(new DnsRequest(new Message(), new ArrayList<>()));
        dnsResender.start();
        Thread.sleep(100);
        assertThat(retried.get(), equalTo(true));
    }
}
