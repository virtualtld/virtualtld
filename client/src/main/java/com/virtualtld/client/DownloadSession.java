package com.virtualtld.client;

import com.protocol.cdc.DecodedHeadNode;
import com.protocol.cdc.DecodedTxtRecord;
import com.protocol.cdc.Password;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.IDN;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class DownloadSession {

    private final static Logger LOGGER = LoggerFactory.getLogger(DownloadSession.class);
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
    private final Consumer<DnsRequest> sendRequest;
    private final BiConsumer<DownloadSession, byte[]> onDownloaded;
    private final Map<Integer, Consumer<Message>> handlers = new HashMap<>();
    private final URI uri;
    private DnsRequest rootNameRequest;
    private DnsRequest tldNameRequest;
    private DnsRequest pathRequest;
    private DecodedSite site;
    private Set<String> bodyChunkReqs = new HashSet<>();
    private Set<String> headNodeReqs = new HashSet<>();
    private Map<Integer, byte[]> bodyChunkResps = new HashMap<>();
    private Password password;
    private Name publicDomain;
    private boolean allHeadNodesReceived;

    public DownloadSession(URI uri, Consumer<DnsRequest> sendRequest,
                           BiConsumer<DownloadSession, byte[]> onDownloaded) {
        this(uri, sendRequest, onDownloaded, ROOT_NAME_SERVERS);

    }

    public DownloadSession(URI uri, Consumer<DnsRequest> sendRequest,
                           BiConsumer<DownloadSession, byte[]> onDownloaded,
                           List<InetSocketAddress> rootNameServers) {
        this.uri = uri;
        this.sendRequest = sendRequest;
        this.onDownloaded = onDownloaded;
        rootNameRequest = createNameRequest(rootNameServers);
        handlers.put(rootNameRequest.getID(), this::onRootNameResponse);
        sendRequest.accept(rootNameRequest);
    }

    private void onRootNameResponse(Message resp) {
        if (tldNameRequest != null) {
            return;
        }
        if (resp.getSectionArray(Section.ANSWER).length > 0) {
            onTldNameResponse(resp);
            return;
        }
        Record[] records = resp.getSectionArray(Section.ADDITIONAL);
        ArrayList<InetSocketAddress> servers = new ArrayList<>();
        for (Record record : records) {
            if (record instanceof ARecord) {
                InetAddress ip = ((ARecord) record).getAddress();
                servers.add(new InetSocketAddress(ip, 53));
            }
        }
        tldNameRequest = createNameRequest(servers);
        handlers.put(tldNameRequest.getID(), this::onTldNameResponse);
        sendRequest.accept(tldNameRequest);
    }

    private void onTldNameResponse(Message resp) {
        if (pathRequest != null) {
            return;
        }
        site = new DecodedSite(resp);
        PathRequest pathRequest = new PathRequest(uri, site.privateDomain());
        LOGGER.info("path request " + pathRequest.digest() + " => " + uri);
        this.pathRequest = new DnsRequest(pathRequest.pathRequest(),
                site.privateResolvers());
        handlers.put(this.pathRequest.getID(), this::onFirstHeadNodeResponse);
        sendRequest.accept(this.pathRequest);
    }

    private void onFirstHeadNodeResponse(Message resp) {
        TXTRecord encodedTxtRecord = (TXTRecord) resp.getSectionArray(Section.ANSWER)[0];
        DecodedTxtRecord decodedTxtRecord = new DecodedTxtRecord(encodedTxtRecord);
        DecodedHeadNode node = new DecodedHeadNode(decodedTxtRecord.data());
        password = new Password(publicDomain.toString(), node.salt());
        processHeadNode(0, node);
    }

    private void onHeadNodeResponse(int bodyChunkIndexBase, Message resp) {
        TXTRecord encodedTxtRecord = (TXTRecord) resp.getSectionArray(Section.ANSWER)[0];
        DecodedTxtRecord decodedTxtRecord = new DecodedTxtRecord(encodedTxtRecord);
        DecodedHeadNode node = new DecodedHeadNode(decodedTxtRecord.data());
        processHeadNode(bodyChunkIndexBase, node);
    }

    private void onBodyChunkResponse(int chunkIndex, Message resp) {
        TXTRecord encodedTxtRecord = (TXTRecord) resp.getSectionArray(Section.ANSWER)[0];
        DecodedTxtRecord decodedTxtRecord = new DecodedTxtRecord(encodedTxtRecord);
        byte[] bytes = password.decrypt(decodedTxtRecord.data());
        bodyChunkResps.put(chunkIndex, bytes);
        LOGGER.info(String.format("received body chunk, allHeadNodesReceived: %s, requestsCount:%d, responsesCount: %d",
                allHeadNodesReceived, bodyChunkReqs.size(), bodyChunkResps.size()));
        if (allHeadNodesReceived && bodyChunkReqs.size() == bodyChunkResps.size()) {
            onDownloaded.accept(this, result());
        }
    }

    private void processHeadNode(int bodyChunkIndexBase, DecodedHeadNode node) {
        List<String> chunkDigests = node.chunkDigests();
        if (node.hasNext()) {
            sendHeadNodeRequest(bodyChunkIndexBase + chunkDigests.size(), node.nextDigest());
        } else {
            LOGGER.info(("all head node received"));
            allHeadNodesReceived = true;
        }
        LOGGER.info("on head node: " + chunkDigests);
        for (int i = 0; i < chunkDigests.size(); i++) {
            sendBodyChunkRequest(i, chunkDigests.get(i));
        }
    }

    private void sendHeadNodeRequest(int bodyChunkIndexBase, String nodeDigest) {
        if (headNodeReqs.contains(nodeDigest)) {
            return;
        }
        headNodeReqs.add(nodeDigest);
        try {
            Record record = Record.newRecord(new Name(nodeDigest, site.privateDomain()),
                    Type.TXT, DClass.IN);
            DnsRequest req = new DnsRequest(Message.newQuery(record), site.privateResolvers());
            handlers.put(req.getID(), resp -> onHeadNodeResponse(bodyChunkIndexBase, resp));
            sendRequest.accept(req);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendBodyChunkRequest(int bodyChunkIndex, String chunkDigest) {
        if (bodyChunkReqs.contains(chunkDigest)) {
            return;
        }
        bodyChunkReqs.add(chunkDigest);
        try {
            Record record = Record.newRecord(new Name(chunkDigest, site.privateDomain()),
                    Type.TXT, DClass.IN);
            DnsRequest req = new DnsRequest(Message.newQuery(record), site.privateResolvers());
            handlers.put(req.getID(), resp -> onBodyChunkResponse(bodyChunkIndex, resp));
            sendRequest.accept(req);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }


    private DnsRequest createNameRequest(List<InetSocketAddress> candidateServers) {
        try {
            publicDomain = Name.fromString(IDN.toASCII(uri.getAuthority() + "."));
            Message message = Message.newQuery(Record.newRecord(publicDomain, Type.NS, DClass.IN));
            return new DnsRequest(message, candidateServers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void onResponse(Message resp) {
        int reqId = resp.getHeader().getID();
        Consumer<Message> handler = handlers.get(reqId);
        if (handler == null) {
            return;
        }
        handler.accept(resp);
        handlers.remove(reqId);
    }

    public byte[] result() {
        int totalSize = 0;
        for (int i = 0; i < bodyChunkResps.size(); i++) {
            totalSize += bodyChunkResps.get(i).length;
        }
        byte[] result = new byte[totalSize];
        int pos = 0;
        for (int i = 0; i < bodyChunkResps.size(); i++) {
            byte[] chunk = bodyChunkResps.get(i);
            System.arraycopy(chunk, 0, result, pos, chunk.length);
            pos += chunk.length;
        }
        return result;
    }
}
