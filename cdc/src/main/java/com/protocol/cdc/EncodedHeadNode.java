package com.protocol.cdc;

import java.util.List;

import static com.protocol.cdc.Digest.hex;
import static com.protocol.cdc.EncodedBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.Password.SALT_SIZE;

public class EncodedHeadNode implements Block {
    public final static byte FLAG_NEXT = (byte) 1;
    public final static byte FLAG_SALT = (byte) 2;

    private final List<EncodedBodyChunk> chunks;
    private EncodedHeadNode next;
    private final byte[] salt;
    private byte[] cache;

    public EncodedHeadNode(List<EncodedBodyChunk> chunks, byte[] salt) {
        this(chunks, salt, null);
    }

    public EncodedHeadNode(List<EncodedBodyChunk> chunks, byte[] salt, EncodedHeadNode next) {
        this.chunks = chunks;
        this.next = next;
        this.salt = salt;
    }

    public void setNext(EncodedHeadNode next) {
        this.next = next;
    }

    private byte nodeFlag() {
        byte flag = 0;
        if (salt != null) {
            flag |= FLAG_SALT;
        }
        if (next != null) {
            flag |= FLAG_NEXT;
        }
        return flag;
    }

    public byte[] data() {
        if (cache == null) {
            cache = calculateData();
        }
        return cache;
    }

    private byte[] calculateData() {
        int size = 1 + chunks.size() * DIGEST_SIZE;
        if (next != null) {
            size += DIGEST_SIZE;
        }
        if (salt != null) {
            size += SALT_SIZE;
        }
        byte[] data = new byte[size];
        int pos = 0;
        data[pos++] = nodeFlag();
        if (next != null) {
            byte[] digest = next.digestBytes();
            System.arraycopy(digest, 0, data, pos, DIGEST_SIZE);
            pos += DIGEST_SIZE;
        }
        if (salt != null) {
            System.arraycopy(salt, 0, data, pos, SALT_SIZE);
            pos += SALT_SIZE;
        }
        for (int i = 0; i < chunks.size(); i++) {
            EncodedBodyChunk chunk = chunks.get(i);
            byte[] digest = chunk.digestBytes();
            System.arraycopy(digest, 0, data, pos, DIGEST_SIZE);
            pos += DIGEST_SIZE;
        }
        return data;
    }

    public byte[] digestBytes() {
        return Digest.sha1Bytes(data());
    }

    public String digest() {
        return Digest.hex(digestBytes());
    }
}
