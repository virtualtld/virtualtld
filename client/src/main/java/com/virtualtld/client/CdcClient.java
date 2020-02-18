package com.virtualtld.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CdcClient {

    private final static Logger LOGGER = LoggerFactory.getLogger("vtld.CdcClient");

    private final DnsClient dnsClient = new DnsClient(this::onDnsClientResponse);

    private final NsCache nsCache = new NsCache(dnsClient::send, this::onResponse);

    private final List<DownloadSession> sessions = new ArrayList<>();

    public final static List<InetSocketAddress> ROOT_NAME_SERVERS = Arrays.asList(
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
    public List<InetSocketAddress> rootNameServers = ROOT_NAME_SERVERS;

    public void start() {
        dnsClient.start();
    }

    public void stop() {
        dnsClient.stop();
    }

    private String onDnsClientResponse(Message resp) {
        return nsCache.onResponse(resp);
    }

    private synchronized String onResponse(Message resp) {
        for (DownloadSession session : new ArrayList<>(sessions)) {
            String result = session.onResponse(resp);
            if (result != null) {
                return result;
            }
        }
        return "no session";
    }

    public synchronized void download(URI uri, OutputStream outputStream) {
        DownloadSession s = new DownloadSession(uri, nsCache::sendRequest, outputStream, (session) -> {
            sessions.remove(session);
            try {
                outputStream.close();
            } catch (IOException e) {
                LOGGER.error("failed to close output stream", e);
            }
        });
        sessions.add(s);
        s.start(rootNameServers);
    }

    public byte[] download(URI uri) {
        try {
            PipedInputStream inputStream = new PipedInputStream();
            download(uri, new PipedOutputStream(inputStream));
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
