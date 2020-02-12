package com.virtualtld.client;

import org.junit.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.net.IDN;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class DownloadSessionTest {

    @Test
    public void happy_path() throws Exception {
        List<DnsRequest> requests = new ArrayList<>();
        DownloadSession session = new DownloadSession(
                new URI("virtualtld://最新版本.com/"), requests::add);
        // request 1
        assertThat(requests, hasSize(1));
        DnsRequest req1 = requests.get(0);
        assertThat(req1.message.getQuestion().getName(),
                equalTo(Name.fromString(IDN.toASCII("最新版本.com."))));
        // response 1
        Message resp1 = new Message(req1.getID());
        resp1.addRecord(Record.newRecord(
                Name.fromString(IDN.toASCII("最新版本.com.")), Type.NS, DClass.IN),
                Section.QUESTION);
        resp1.addRecord(new ARecord(Name.fromString("a.gtld-servers.net."), DClass.IN, 172800,
                Inet4Address.getByName("192.5.6.30")), Section.ADDITIONAL);
        session.onResponse(resp1);
        // request 2
        assertThat(requests, hasSize(2));
        DnsRequest req2 = requests.get(1);
        assertThat(req2.message.getQuestion().getName(),
                equalTo(Name.fromString(IDN.toASCII("最新版本.com."))));
        List<InetSocketAddress> servers = Collections.singletonList(
                new InetSocketAddress("192.5.6.30", 53));
        assertThat(req2.candidateServers, equalTo(servers));
        // repsonse 2
        Message resp2 = new Message(req2.getID());
        resp2.addRecord(Record.newRecord(
                Name.fromString(IDN.toASCII("最新版本.com.")), Type.NS, DClass.IN),
                Section.QUESTION);
        resp2.addRecord(new NSRecord(Name.fromString("最新版本.com."), DClass.IN, 172800,
                Name.fromString("ver.1.1.virtualtld.com.")), Section.ANSWER);
        resp2.addRecord(new NSRecord(Name.fromString("最新版本.com."), DClass.IN, 172800,
                Name.fromString("xn--efv12a2dz86b.xyz.virtualtld.com.")), Section.ANSWER);
        resp2.addRecord(new NSRecord(Name.fromString("最新版本.com."), DClass.IN, 172800,
                Name.fromString("1.1.1.1.virtualtld.com.")), Section.ANSWER);
        session.onResponse(resp2);
        // request 3
        assertThat(requests, hasSize(3));
        DnsRequest req3 = requests.get(2);
        assertThat(req3.message.getQuestion().getName(),
                equalTo(Name.fromString(IDN.toASCII("r8eMbUUdjj2cPfqeMwBJG8amEPQ=.最新版本.xyz."))));
    }
}
