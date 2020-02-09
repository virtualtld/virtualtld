package com.virtualtld.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.net.InetSocketAddress;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Executors;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class DnsClientTest {

    private DnsClient dnsClient;
    private Exchanger<Message> respExchange = new Exchanger<>();

    @Before
    public void setup() {
        dnsClient = new DnsClient((resp) -> {
            try {
                respExchange.exchange(resp);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Executors.newFixedThreadPool(1).submit(dnsClient::start);
    }

    @After
    public void teardown() {
        dnsClient.stop();
    }

    @Test
    public void query_once() throws Exception {
        Record record = Record.newRecord(Name.fromString("microsoft.com."), Type.A, DClass.IN);
        dnsClient.accept(Message.newQuery(record), new InetSocketAddress("1.1.1.1", 53));
        Message resp = respExchange.exchange(null);
        assertThat(resp.getSectionArray(Section.ANSWER), not(emptyArray()));

    }
}
