package com.protocol.cdc;

import static com.protocol.cdc.Digest.base64;

public class EncodedBodyChunk implements Block {
    public static final int DIGEST_SIZE = 20;
    private final Password password;
    private final byte[] decodedData;
    private byte[] cache;

    public EncodedBodyChunk(Password password, byte[] decodedData) {
        this.password = password;
        this.decodedData = decodedData;
    }

    public byte[] decodedData() {
        return this.decodedData;
    }

    public byte[] data() {
        if (cache == null) {
            cache = calculateData();
        }
        return cache;
    }
    
    private byte[] calculateData() {
        return password.encrypt(decodedData);
    }

    public byte[] digestBytes() {
        return Digest.sha1Bytes(data());
    }

    public String digest() {
        return base64(digestBytes());
    }
}
