package com.virtualtld.server;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.net.IDN;
import java.net.InetSocketAddress;

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
        nsResp.getHeader().setFlag(Flags.QR);
        nsResp.getHeader().setFlag(Flags.AA);
        nsResp.addRecord(Record.newRecord(name, Type.NS, DClass.IN), Section.QUESTION);
        addRecord(new Name("ver." + conf.majorVersion + "." + conf.minorVersion, name));
        addRecord(new Name(IDN.toASCII(conf.privateDomain), name));
        for (InetSocketAddress privateResolver : conf.privateResolvers) {
            String ip = privateResolver.getAddress().toString().substring(1);
            addRecord(new Name(ip + "." + privateResolver.getPort(), name));
        }
        return nsResp;
    }

    private void addRecord(Name target) {
        nsResp.addRecord(new NSRecord(this.name, DClass.IN, 4090, target), Section.ANSWER);
    }
}
