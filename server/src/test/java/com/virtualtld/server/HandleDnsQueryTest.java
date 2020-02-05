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
import java.util.HashMap;

public class HandleDnsQueryTest {

    @Test
    public void query_ns() throws Exception {
        Name name = Name.fromString(IDN.toASCII("最新版本.com."));
        Message input = Message.newQuery(Record.newRecord(name, Type.NS, DClass.IN));
        input.getHeader().setID(1029);
        Message nsResp = new Message();
        nsResp.addRecord(Record.newRecord(name, Type.NS, DClass.IN, 1024,
                Name.fromString("ver.1.1.virtualtld.com.").toWire()), Section.ANSWER);
        Message output = new HandleDnsQuery(nsResp, new HashMap<>()).apply(input);
        nsResp.getHeader().setID(1029);
        Assert.assertEquals(nsResp.toString(), output.toString());
    }

    @Test
    public void query_txt_less_than_255() throws Exception {
        Name name = Name.fromString(IDN.toASCII("abcd.最新版本.xyz."));
        Message input = Message.newQuery(Record.newRecord(name, Type.TXT, DClass.IN));
        Message output = new HandleDnsQuery(null, new HashMap<String, byte[]>() {{
            put("abcd", new byte[]{1, 2, 3, 4});
        }}).apply(input);
        System.out.println(output);
        Assert.assertNotEquals(-1, output.toString().indexOf("\"\\001\\002\\003\\004\""));
    }
}
