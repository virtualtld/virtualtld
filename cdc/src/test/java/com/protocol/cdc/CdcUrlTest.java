package com.protocol.cdc;

import com.protocol.dns.DnsName;

import org.junit.Assert;
import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.IDN;
import java.net.InetAddress;

public class CdcUrlTest {
    @Test
    public void test_encode() {
        CdcSite site = new CdcSite("最新版本.com", "最新版本.xyz");
        DnsName dnsName = CdcUrl.encode(site, "");
        Assert.assertEquals("n7B5G0YvpBA0elSBz5OLbBTbJnU=.xn--efv12a2dz86b.xyz", dnsName.toString());
    }

    @Test
    public void test_encode_dns() throws Exception {
        Message msg = Message.newQuery(Record.newRecord(Name.fromString(IDN.toASCII("1最新版本.com.")), Type.NS, DClass.IN));
        msg.getHeader().setID(1029);
        msg.getHeader().unsetFlag(Flags.RD);
        byte[] req = msg.toWire();
        System.out.println(req.length);
        DatagramPacket reqPacket = new DatagramPacket(req, req.length, InetAddress.getByName("192.12.94.30"), 53);
        DatagramSocket sock = new DatagramSocket();
        sock.send(reqPacket);
        DatagramPacket resp = new DatagramPacket(new byte[512], 512);
        sock.receive(resp);
        Message respMsg = new Message(resp.getData());
        System.out.println(respMsg.getHeader().printFlags());
        System.out.println(respMsg.getHeader().getOpcode());
        System.out.println(respMsg.getHeader().getID());
        System.out.println(respMsg);
    }
}
