package com.virtualtld.client;

import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Exchanger;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ResilientDnsClientTest {

    @Test
    public void without_retry() throws Exception {
        Exchanger<Message> respExchange = new Exchanger<>();
        ResilientDnsClient dnsClient = new ResilientDnsClient(resp -> {
            try {
                respExchange.exchange(resp);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        dnsClient.start();
        Record record = Record.newRecord(Name.fromString("microsoft.com."), Type.A, DClass.IN);
        dnsClient.send(Message.newQuery(record));
        Message resp = respExchange.exchange(null);
        assertThat(resp.getSectionArray(Section.ANSWER), not(emptyArray()));
        dnsClient.stop();
    }

    @Test
    public void with_retry() throws Exception {
        Exchanger<Message> respExchange = new Exchanger<>();
        ArrayList<Message> sentMessages = new ArrayList<>();
        ResilientDnsClient dnsClient = new ResilientDnsClient(resp -> {
            try {
                respExchange.exchange(resp);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }) {
            @Override
            protected DnsClient newDnsClient(Consumer<Message> onResponse) {
                return new DnsClient(onResponse) {
                    @Override
                    public void send(Message req, InetSocketAddress resolverAddr) {
                        if (sentMessages.size() != 0) {
                            super.send(req, resolverAddr);
                        }
                        sentMessages.add(req);
                    }
                };
            }
        };
        dnsClient.start();
        Record record = Record.newRecord(Name.fromString("microsoft.com."), Type.A, DClass.IN);
        dnsClient.send(Message.newQuery(record));
        Message resp = respExchange.exchange(null);
        assertThat(resp.getSectionArray(Section.ANSWER), not(emptyArray()));
        dnsClient.stop();
    }
}
