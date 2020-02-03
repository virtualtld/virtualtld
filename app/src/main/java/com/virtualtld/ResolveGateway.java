package com.virtualtld;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.protocol.dns.DnsName;
import com.protocol.dns.Header;
import com.protocol.dns.MakeQueryPacket;
import com.protocol.dns.Packet;
import com.protocol.dns.ResourceRecord;
import com.protocol.dns.ResourceRecords;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Function;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ResolveGateway implements Function<String, Gateway> {
    @Override
    public Gateway apply(String domainName) {
        try {
            return _apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Gateway _apply() throws Exception {
        Packet packet = new MakeQueryPacket().apply(new MakeQueryPacket.Req() {{
            fqdn = new DnsName("body.xn--efv12a2dz86b.com");
            xid = 1024;
            qclass = ResourceRecord.CLASS_INTERNET;
            qtype = ResourceRecord.TYPE_TXT;
            recursion = true;
        }});
        DatagramSocket sock = new DatagramSocket();
        sock.send(new DatagramPacket(packet.getData(), packet.length(), InetAddress.getByName("127.0.0.1"), 53));
        DatagramPacket received = new DatagramPacket(new byte[512], 512);
        sock.receive(received);
        Header header = new Header(received.getData(), received.getLength());
        System.out.println(header.truncated);
        System.out.println(received.getLength());
        ResourceRecords records = new ResourceRecords(received.getData(), received.getLength(), header, false);
        System.out.println(records.answer.get(0));
        System.out.println(records.answer.get(1));
        return null;
    }
}
