package com.virtualtld.client;

import com.protocol.cdc.Block;
import com.protocol.cdc.EncodedFile;
import com.protocol.cdc.EncodedTxtRecord;
import com.protocol.cdc.VirtualtldSite;

import org.junit.Test;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Section;

import java.net.IDN;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class DownloadSessionTest {

    private boolean downloaded;

    @Test
    public void happy_path() throws Exception {
        EncodedFile encodedFile = new EncodedFile(new VirtualtldSite(
                "最新版本.com", "最新版本.xyz"),
                "/", "hello".getBytes());
        HashMap<String, Block> blocks = new HashMap<>();
        for (Block block : encodedFile.blocks()) {
            blocks.put(block.digest(), block);
        }
        List<DnsRequest> requests = new ArrayList<>();
        DownloadSession session = new DownloadSession(
                new URI("virtualtld://最新版本.com/"), requests::add, (s, result) -> {
            downloaded = true;
            assertThat(new String(result), equalTo("hello"));
        });
        // request 1
        assertThat(requests, hasSize(1));
        DnsRequest req1 = requests.get(0);
        assertThat(req1.message.getQuestion().getName(),
                equalTo(Name.fromString(IDN.toASCII("最新版本.com."))));
        // response 1
        Message resp1 = new Message(req1.getID());
        resp1.addRecord(req1.message.getQuestion(), Section.QUESTION);
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
        resp2.addRecord(req2.message.getQuestion(), Section.QUESTION);
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
                equalTo(Name.fromString(IDN.toASCII("afc78c6d451d8e3d9c3dfa9e3300491bc6a610f4.最新版本.xyz."))));
        // response 3
        replyBlock(session, blocks, req3);
        // request 4
        assertThat(requests, hasSize(4));
        DnsRequest req4 = requests.get(3);
        assertThat(req4.message.getQuestion().getName(),
                equalTo(Name.fromString(IDN.toASCII("9df6145647cc3a01965b5ee8b44a9823a9b0dcbb.最新版本.xyz."))));
        // response 4
        replyBlock(session, blocks, req4);
        assertThat(downloaded, equalTo(true));

    }

    private void replyBlock(DownloadSession session, HashMap<String, Block> blocks, DnsRequest req3) {
        Message resp3 = new Message(req3.getID());
        resp3.addRecord(req3.message.getQuestion(), Section.QUESTION);
        Name reqName = req3.message.getQuestion().getName();
        Block block = blocks.get(reqName.getLabelString(0));
        resp3.addRecord(new EncodedTxtRecord(reqName, block.data()).txtRecord(), Section.ANSWER);
        session.onResponse(resp3);
    }
}
