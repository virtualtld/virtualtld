package com.protocol.cdc;

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
            cache = password.encrypt(decodedData);
        }
        return cache;
    }

    @Override
    public int ttl() {
        return 172800;
    }

    public byte[] digestBytes() {
        return Digest.sha1Bytes(data());
    }

    public String digest() {
        return Digest.hex(digestBytes());
    }
}
