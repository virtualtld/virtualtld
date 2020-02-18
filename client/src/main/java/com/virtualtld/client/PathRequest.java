package com.virtualtld.client;

import com.protocol.cdc.EncodedPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.net.IDN;
import java.net.URI;

public class PathRequest {

    private final URI uri;
    private final Name privateDomain;

    public PathRequest(URI uri, Name privateDomain) {
        this.uri = uri;
        this.privateDomain = privateDomain;
    }

    public Message pathRequest() {
        try {
            String digest = digest();
            return Message.newQuery(Record.newRecord(
                    new Name(digest, privateDomain), Type.TXT, DClass.IN));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String digest() {
        try {
            Name publicDomain = Name.fromString(IDN.toASCII(uri.getAuthority() + "."));
            return new EncodedPath(publicDomain, uri.getPath()).digest();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

