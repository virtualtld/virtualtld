package com.protocol.cdc;

import com.protocol.dns.DnsName;

public class CdcUrl {
    public static DnsName encode(CdcSite site, String path) {
        path = path.length() > 0 ? path : "/";
        String digest = Digest.sha1(site.publicDomain.getBytes(), path.getBytes());
        return new DnsName(digest + "." + site.privateDomain);
    }
}
