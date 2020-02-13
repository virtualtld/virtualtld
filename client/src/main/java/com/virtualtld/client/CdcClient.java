package com.virtualtld.client;


import org.xbill.DNS.Message;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CdcClient {

    private final DnsClient dnsClient = new DnsClient(this::onDnsClientResponse);

    private final NsCache nsCache = new NsCache(dnsClient::send, this::onResponse);

    private final List<DownloadSession> sessions = new ArrayList<>();

    public List<InetSocketAddress> rootNameServers = DownloadSession.ROOT_NAME_SERVERS;

    public void start() {
        dnsClient.start();
    }

    public void stop() {
        dnsClient.stop();
    }

    private void onDnsClientResponse(Message resp) {
        nsCache.onResponse(resp);
    }

    private synchronized void onResponse(Message resp) {
        for (DownloadSession session : new ArrayList<>(sessions)) {
            session.onResponse(resp);
        }
    }

    public synchronized void download(URI uri, Consumer<byte[]> callback) {
        sessions.add(new DownloadSession(uri, nsCache::sendRequest, (session, result) -> {
            sessions.remove(session);
            callback.accept(result);
        }, rootNameServers));
    }
}
