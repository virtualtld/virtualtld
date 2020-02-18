package com.virtualtld.client;

import com.protocol.cdc.DecodedHeadNode;
import com.protocol.cdc.DecodedTxtRecord;
import com.protocol.cdc.Digest;
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.IDN;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;


public class DownloadSession {

    private final static Logger LOGGER = LoggerFactory.getLogger("vtld.DownloadSession");
    private final Consumer<DnsRequest> sendRequest;
    private final OutputStream outputStream;
    private final Consumer<DownloadSession> onDownloaded;
    private final Map<Integer, Function<Message, String>> handlers = new HashMap<>();
    private final URI uri;
    private DnsRequest rootNameRequest;
    private DnsRequest tldNameRequest;
    private DnsRequest pathRequest;
    private DecodedSite site;
    private Set<String> bodyChunkReqs = new HashSet<>();
    private Set<String> missingBodyChunkDigests = new HashSet<>();
    private Set<String> headNodeReqs = new HashSet<>();
    private Map<Integer, byte[]> bodyChunkResps = new HashMap<>();
    private Password password;
    private Name publicDomain;
    private boolean allHeadNodesReceived;

    public DownloadSession(URI uri, Consumer<DnsRequest> sendRequest, OutputStream outputStream, Consumer<DownloadSession> onDownloaded) {
        this.uri = uri;
        this.sendRequest = sendRequest;
        this.outputStream = outputStream;
        this.onDownloaded = onDownloaded;
    }

    public void start(List<InetSocketAddress> rootNameServers) {
        rootNameRequest = createNameRequest(rootNameServers);
        addHandler(rootNameRequest, this::onRootNameResponse);
        sendRequest.accept(rootNameRequest);
    }

    private synchronized String onRootNameResponse(Message resp) {
        if (tldNameRequest != null) {
            return "already sent tldNameRequest";
        }
        if (resp.getSectionArray(Section.ANSWER).length > 0) {
            return onTldNameResponse(resp);
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
        addHandler(tldNameRequest, this::onTldNameResponse);
        sendRequest.accept(tldNameRequest);
        return "success";
    }

    private synchronized String onTldNameResponse(Message resp) {
        if (pathRequest != null) {
            return "already sent pathRequest";
        }
        site = new DecodedSite(resp);
        PathRequest pathRequest = new PathRequest(uri, site.privateDomain());
        LOGGER.info("path request " + pathRequest.digest() + " => " + uri);
        this.pathRequest = new DnsRequest(pathRequest.pathRequest(),
                site.privateResolvers());
        addHandler(this.pathRequest, this::onFirstHeadNodeResponse);
        sendRequest.accept(this.pathRequest);
        return "success";
    }

    private String onFirstHeadNodeResponse(Message resp) {
        TXTRecord encodedTxtRecord = (TXTRecord) resp.getSectionArray(Section.ANSWER)[0];
        byte[] data = new DecodedTxtRecord(encodedTxtRecord).data();
        DecodedHeadNode node = new DecodedHeadNode(data);
        password = new Password(publicDomain.toString(), node.salt());
        processHeadNode(0, node);
        return "success";
    }

    private byte[] decodeTxtRecord(TXTRecord encodedTxtRecord) {
        String receivedDigest = encodedTxtRecord.getName().getLabelString(0);
        DecodedTxtRecord decodedTxtRecord = new DecodedTxtRecord(encodedTxtRecord);
        byte[] bytes = decodedTxtRecord.data();
        String actualDigest = Digest.sha1(bytes);
        if (!receivedDigest.equals(actualDigest)) {
            throw new RuntimeException(encodedTxtRecord.getName() + " content digest invalid: " + actualDigest);
        }
        return bytes;
    }

    private String onHeadNodeResponse(int bodyChunkIndexBase, Message resp) {
        TXTRecord encodedTxtRecord = (TXTRecord) resp.getSectionArray(Section.ANSWER)[0];
        byte[] data = decodeTxtRecord(encodedTxtRecord);
        DecodedHeadNode node = new DecodedHeadNode(data);
        processHeadNode(bodyChunkIndexBase, node);
        return "success";
    }

    private synchronized String onBodyChunkResponse(int chunkIndex, Message resp) {
        String receivedDigest = resp.getQuestion().getName().getLabelString(0);
        missingBodyChunkDigests.remove(receivedDigest);
        TXTRecord encodedTxtRecord = (TXTRecord) resp.getSectionArray(Section.ANSWER)[0];
        byte[] bytes = password.decrypt(decodeTxtRecord(encodedTxtRecord));
        bodyChunkResps.put(chunkIndex, bytes);
        if (missingBodyChunkDigests.size() > 0 && missingBodyChunkDigests.size() < 3) {
            LOGGER.info("still missing " + missingBodyChunkDigests);
        }
        LOGGER.info(String.format("received body chunk@%d, allHeadNodesReceived: %s, requestsCount:%d, responsesCount: %d",
                chunkIndex, allHeadNodesReceived, bodyChunkReqs.size(), bodyChunkResps.size()));
        if (allHeadNodesReceived && missingBodyChunkDigests.isEmpty()) {
            try {
                outputStream.write(result());
                onDownloaded.accept(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return "success";
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
            sendBodyChunkRequest(bodyChunkIndexBase + i, chunkDigests.get(i));
        }
    }

    private synchronized void sendHeadNodeRequest(int bodyChunkIndexBase, String nodeDigest) {
        if (headNodeReqs.contains(nodeDigest)) {
            return;
        }
        headNodeReqs.add(nodeDigest);
        try {
            Record record = Record.newRecord(new Name(nodeDigest, site.privateDomain()),
                    Type.TXT, DClass.IN);
            DnsRequest req = new DnsRequest(Message.newQuery(record), site.privateResolvers());
            addHandler(req, resp -> onHeadNodeResponse(bodyChunkIndexBase, resp));
            sendRequest.accept(req);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void sendBodyChunkRequest(int bodyChunkIndex, String chunkDigest) {
        if (bodyChunkReqs.contains(chunkDigest)) {
            return;
        }
        try {
            Record record = Record.newRecord(new Name(chunkDigest, site.privateDomain()),
                    Type.TXT, DClass.IN);
            DnsRequest req = new DnsRequest(Message.newQuery(record), site.privateResolvers());
            LOGGER.info("add handler for " + req.message.getQuestion().getName() + "@" + bodyChunkIndex + " with id " + req.getID());
            addHandler(req, resp -> onBodyChunkResponse(bodyChunkIndex, resp));
            bodyChunkReqs.add(chunkDigest);
            missingBodyChunkDigests.add(chunkDigest);
            sendRequest.accept(req);
        } catch (Exception e) {
            LOGGER.error("failed to send request " + chunkDigest, e);
        }
    }

    private Function<Message, String> addHandler(DnsRequest req, Function<Message, String> handler) {
        while (handlers.containsKey(req.getID())) {
            req.message.getHeader().setID(Math.abs((short)(req.getID() * 31)));
        }
        return handlers.put(req.getID(), handler);
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

    public synchronized String onResponse(Message resp) {
        int reqId = resp.getHeader().getID();
        Function<Message, String> handler = handlers.get(reqId);
        if (handler == null) {
            return null;
        }
        String result = handler.apply(resp);
        handlers.remove(reqId);
        return result;
    }

    public byte[] result() {
        int totalSize = 0;
        for (int i = 0; i < bodyChunkResps.size(); i++) {
            byte[] bytes = bodyChunkResps.get(i);
            if (bytes == null) {
                LOGGER.error("missing chunk " + i);
            }
            totalSize += bytes.length;
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
