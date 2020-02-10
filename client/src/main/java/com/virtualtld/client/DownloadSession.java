package com.virtualtld.client;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.net.IDN;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class DownloadSession {

    private final static List<InetSocketAddress> ROOT_NAME_SERVERS = Arrays.asList(
            new InetSocketAddress("198.41.0.4", 53),
            new InetSocketAddress("199.9.14.201", 53),
            new InetSocketAddress("192.33.4.12", 53),
            new InetSocketAddress("199.7.91.13", 53),
            new InetSocketAddress("192.203.230.10", 53),
            new InetSocketAddress("192.5.5.241", 53),
            new InetSocketAddress("192.112.36.4", 53),
            new InetSocketAddress("198.97.190.53", 53),
            new InetSocketAddress("192.36.148.17", 53),
            new InetSocketAddress("192.58.128.30", 53),
            new InetSocketAddress("193.0.14.129", 53),
            new InetSocketAddress("199.7.83.42", 53),
            new InetSocketAddress("202.12.27.33", 53)
    );
    private final Consumer<DnsRequest> sendRequest;
    private final URI uri;

    public DownloadSession(URI uri, Consumer<DnsRequest> sendRequest) {
        this.uri = uri;
        this.sendRequest = sendRequest;
        sendRequest.accept(initialRequest());
    }

    private DnsRequest initialRequest() {
        try {
            Name name = Name.fromString(IDN.toASCII(uri.getAuthority() + "."));
            Message message = Message.newQuery(Record.newRecord(name, Type.NS, DClass.IN));
            return new DnsRequest(message, ROOT_NAME_SERVERS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onResponse(Message resp) {
    }

    public byte[] result() {
        throw new RuntimeException();
    }
}
