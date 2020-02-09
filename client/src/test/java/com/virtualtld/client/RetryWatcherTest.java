package com.virtualtld.client;

import org.junit.Test;
import org.xbill.DNS.Message;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RetryWatcherTest {

    @Test
    public void resend_after_timeout() throws Exception {
        AtomicBoolean retried = new AtomicBoolean(false);
        RetryWatcher retryWatcher = new RetryWatcher((msg, addr) -> {
            retried.set(true);
        }, new int[]{1});
        retryWatcher.addItem(new RetryWatcher.Item(
                new Message(), new ArrayList<>(), 0));
        retryWatcher.start();
        Thread.sleep(100);
        assertThat(retried.get(), equalTo(true));
    }
}
