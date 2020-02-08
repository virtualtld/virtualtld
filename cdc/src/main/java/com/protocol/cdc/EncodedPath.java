package com.protocol.cdc;

import org.xbill.DNS.Name;

public class EncodedPath implements Block {

    private final Name publicDomain;
    private final String path;
    private final EncodedHeadNode firstNode;

    public EncodedPath(Name publicDomain, String path, EncodedHeadNode firstNode) {
        this.publicDomain = publicDomain;
        this.path = path;
        this.firstNode = firstNode;
    }

    public String digest() {
        String normalizedPath = path.length() > 0 ? path : "/";
        return Digest.sha1(publicDomain.toString().getBytes(),
                normalizedPath.getBytes());
    }

    public byte[] data() {
        return firstNode.data();
    }
}
