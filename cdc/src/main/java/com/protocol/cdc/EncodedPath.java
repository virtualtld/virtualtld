package com.protocol.cdc;

import org.xbill.DNS.Name;

public class EncodedPath {

    private final Name publicDomain;
    private final String path;

    public EncodedPath(Name publicDomain, String path) {
        this.publicDomain = publicDomain;
        this.path = path;
    }

    public String digest() {
        String normalizedPath = path.length() > 0 ? path : "/";
        return Digest.sha1(publicDomain.toString().getBytes(),
                normalizedPath.getBytes());
    }
}
