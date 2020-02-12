package com.virtualtld.client;

import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.net.IDN;
import java.net.InetSocketAddress;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DecodedSiteTest {

    @Test
    public void valid_response() throws Exception {
        Message resp = new Message(0);
        resp.addRecord(Record.newRecord(
                Name.fromString(IDN.toASCII("最新版本.com.")), Type.NS, DClass.IN),
                Section.QUESTION);
        resp.addRecord(new NSRecord(Name.fromString("最新版本.com."), DClass.IN, 172800,
                Name.fromString("ver.1.1.virtualtld.com.")), Section.ANSWER);
        resp.addRecord(new NSRecord(Name.fromString("最新版本.com."), DClass.IN, 172800,
                Name.fromString("xn--efv12a2dz86b.xyz.virtualtld.com.")), Section.ANSWER);
        resp.addRecord(new NSRecord(Name.fromString("最新版本.com."), DClass.IN, 172800,
                Name.fromString("1.1.1.1.virtualtld.com.")), Section.ANSWER);
        DecodedSite decodedSite = new DecodedSite(resp);
        assertThat(decodedSite.privateDomain(), equalTo(Name.fromString("xn--efv12a2dz86b.xyz.")));
        assertThat(decodedSite.privateResolvers(), equalTo(Collections.singletonList(
                new InetSocketAddress("1.1.1.1", 53)
        )));
    }
}
