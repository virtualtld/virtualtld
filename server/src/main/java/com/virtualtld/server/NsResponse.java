package com.virtualtld.server;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.IDN;

public class NsResponse {

    private final VirtualTldConf conf;
    private Name name;
    private Message nsResp;

    public NsResponse(VirtualTldConf conf) {
        this.conf = conf;
    }

    public Message nsResponse() {
        try {
            return _nsResponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Message _nsResponse() throws Exception {
        name = Name.fromString(IDN.toASCII(conf.publicDomain + "."));
        nsResp = new Message();
        addRecord("ver." + conf.majorVersion + "." + conf.minorVersion + ".virtualtld.com.");
        return nsResp;
    }

    private void addRecord(String name) throws TextParseException {
        nsResp.addRecord(Record.newRecord(this.name, Type.NS, DClass.IN, 4090, Name.fromString(name).toWire()), Section.ANSWER);
    }
}
