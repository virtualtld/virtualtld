package com.protocol.cdc;

public class CdcFileBodyChunk {
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
