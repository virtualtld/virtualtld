package com.virtualtld.client;

import com.protocol.cdc.EncodedPath;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.net.IDN;
import java.net.URI;

public class NsRequest {

    private final URI uri;

    public NsRequest(URI uri) {
        this.uri = uri;
    }

    public Message nsRequest() {
        try {
            return _nsRequest();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Message _nsRequest() throws Exception {
        if (!uri.getScheme().equals("virtualtld")) {
            throw new IllegalArgumentException("protocol must be virtualtld");
        }
        Name publicDomain = Name.fromString(IDN.toASCII(uri.getAuthority() + "."));
        String digest = new EncodedPath(publicDomain, uri.getPath()).digest();
        return Message.newQuery(Record.newRecord(
                new Name(digest, publicDomain), Type.NS, DClass.IN));
    }
}

