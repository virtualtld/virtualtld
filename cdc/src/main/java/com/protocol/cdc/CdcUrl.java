package com.protocol.cdc;

import com.protocol.dns.DnsName;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CdcUrl {
    public static DnsName encode(CdcSite site, String path) {
        path = path.length() > 0 ? path : "/";
        MessageDigest crypt = sha1();
        crypt.reset();
        crypt.update(site.publicDomain.getBytes());
        crypt.update(path.getBytes());
        String digest = Base64.getEncoder().encodeToString(crypt.digest());
        return new DnsName(digest + "." + site.privateDomain);
    }

    private static MessageDigest sha1() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
