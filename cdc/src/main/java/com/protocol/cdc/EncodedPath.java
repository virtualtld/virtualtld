package com.protocol.cdc;

import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

public class EncodedPath {

    private final CdcSite site;
    private final String path;

    public EncodedPath(CdcSite site, String path) {
        this.site = site;
        this.path = path;
    }

    public String digest() {
        String normalizedPath = path.length() > 0 ? path : "/";
        return Digest.sha1(site.publicDomain.toString().getBytes(),
                normalizedPath.getBytes());
    }

    public Name dnsName() {
        try {
            return new Name(digest(), site.privateDomain);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }
}
