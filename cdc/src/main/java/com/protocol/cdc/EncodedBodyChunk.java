package com.protocol.cdc;

import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

import static com.protocol.cdc.Digest.base64;

public class EncodedBodyChunk {
    public static final int DIGEST_SIZE = 20;
    private final Name privateDomain;
    private final Password password;
    private final byte[] decodedData;

    public EncodedBodyChunk(Name privateDomain, Password password, byte[] decodedData) {
        this.privateDomain = privateDomain;
        this.password = password;
        this.decodedData = decodedData;
    }

    public byte[] decodedData() {
        return this.decodedData;
    }

    public byte[] data() {
        return password.encrypt(decodedData);
    }

    public byte[] digest() {
        return Digest.sha1Bytes(data());
    }

    public Name dnsName() {
        try {
            return new Name(base64(digest()), privateDomain);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }
}
