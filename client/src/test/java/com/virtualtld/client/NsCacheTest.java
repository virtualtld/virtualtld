package com.virtualtld.client;

import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NsCacheTest {

    @Test
    public void ns_response_can_be_cached() throws Exception {
        ArrayList<DnsRequest> requests = new ArrayList<>();
        ArrayList<Message> responses = new ArrayList<>();
        NsCache nsCache = new NsCache(requests::add, resp -> {
            responses.add(resp);
            return "";
        });
        Message resp = new Message();
        resp.addRecord(Record.newRecord(Name.fromString("abc.com."), Type.NS, DClass.IN),
                Section.QUESTION);
        resp.addRecord(new NSRecord(Name.fromString("abc.com."), DClass.IN, 172800,
                Name.fromString("ver.1.1.virtualtld.com.")), Section.ANSWER);
        nsCache.onResponse(resp);
        nsCache.sendRequest(new DnsRequest(Message.newQuery(
                Record.newRecord(Name.fromString("abc.com."), Type.NS, DClass.IN)),
                new ArrayList<>()));
        assertThat(requests.size(), equalTo(0));
        assertThat(responses.size(), equalTo(2));
    }
}
