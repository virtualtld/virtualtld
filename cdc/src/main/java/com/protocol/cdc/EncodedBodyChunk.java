package com.protocol.cdc;

public class EncodedBodyChunk {
    public static final int DIGEST_SIZE = 20;
    private final Password password;
    private final byte[] decodedData;

    public EncodedBodyChunk(Password password, byte[] decodedData) {
        this.password = password;
        this.decodedData = decodedData;
    }

    public byte[] digest() {
        return Digest.sha1Bytes(data());
    }

    public byte[] decodedData() {
        return this.decodedData;
    }

    public byte[] data() {
        return password.encrypt(decodedData);
    }
}
