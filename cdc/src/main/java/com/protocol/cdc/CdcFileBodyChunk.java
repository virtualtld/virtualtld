package com.protocol.cdc;

public class CdcFileBodyChunk {
    public static final int DIGEST_SIZE = 20;
    private final byte[] data;

    public CdcFileBodyChunk(byte[] data) {
        this.data = data;
    }

    public byte[] digest() {
        return Digest.sha1Bytes(data);
    }

    public byte[] data() {
        return this.data;
    }
}
