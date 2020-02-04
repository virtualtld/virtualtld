package com.virtualtld.server;

import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.net.IDN;

public class HandleDnsQueryTest {

    @Test
    public void query_ns() throws Exception {
        Name name = Name.fromString(IDN.toASCII("最新版本.com."));
        Message input = Message.newQuery(Record.newRecord(name, Type.NS, DClass.IN));
        input.getHeader().setID(1029);
        Message nsResp = new Message();
        nsResp.addRecord(Record.newRecord(name, Type.NS, DClass.IN, 1024,
                Name.fromString("ver.1.1.virtualtld.com.").toWire()), Section.ANSWER);
        Message output = new HandleDnsQuery(nsResp).apply(input);
        nsResp.getHeader().setID(1029);
        Assert.assertEquals(nsResp.toString(), output.toString());
    }
}
