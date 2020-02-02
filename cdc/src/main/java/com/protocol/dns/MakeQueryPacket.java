package com.protocol.dns;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.net.DatagramPacket;
import java.util.function.Function;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MakeQueryPacket implements Function<MakeQueryPacket.Req, Packet> {

    // DNS packet header field offsets
    private static final int IDENT_OFFSET = 0;
    private static final int FLAGS_OFFSET = 2;
    private static final int NUMQ_OFFSET = 4;
    private static final int NUMANS_OFFSET = 6;
    private static final int NUMAUTH_OFFSET = 8;
    private static final int DNS_HDR_SIZE = 12;

    static Packet makeQueryPacket(DnsName fqdn, int xid,
                                  int qclass, int qtype, boolean recursion) {
        int qnameLen = fqdn.getOctets();
        int pktLen = DNS_HDR_SIZE + qnameLen + 4;
        Packet pkt = new Packet(pktLen);

        short flags = recursion ? Header.RD_BIT : 0;

        pkt.putShort(xid, IDENT_OFFSET);
        pkt.putShort(flags, FLAGS_OFFSET);
        pkt.putShort(1, NUMQ_OFFSET);
        pkt.putShort(0, NUMANS_OFFSET);
        pkt.putInt(0, NUMAUTH_OFFSET);

        makeQueryName(fqdn, pkt, DNS_HDR_SIZE);
        pkt.putShort(qtype, DNS_HDR_SIZE + qnameLen);
        pkt.putShort(qclass, DNS_HDR_SIZE + qnameLen + 2);

        return pkt;
    }

    // Builds a query name in pkt according to the RFC spec.
    private static void makeQueryName(DnsName fqdn, Packet pkt, int off) {

        // Loop through labels, least-significant first.
        for (int i = fqdn.size() - 1; i >= 0; i--) {
            String label = fqdn.get(i);
            int len = label.length();

            pkt.putByte(len, off++);
            for (int j = 0; j < len; j++) {
                pkt.putByte(label.charAt(j), off++);
            }
        }
        if (!fqdn.hasRootLabel()) {
            pkt.putByte(0, off);
        }
    }

    @Override
    public Packet apply(Req req) {
        return makeQueryPacket(req.fqdn, req.xid, req.qclass, req.qtype, req.recursion);
    }

    public static class Req {
        public DnsName fqdn;
        public int xid;
        public int qclass;
        public int qtype;
        public boolean recursion;
    }
}