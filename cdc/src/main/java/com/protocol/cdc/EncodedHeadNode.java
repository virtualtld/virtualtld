package com.protocol.cdc;

import java.util.List;

import static com.protocol.cdc.EncodedBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.Password.SALT_SIZE;

class EncodedHeadNode {
    public final static byte WITH_NEXT = (byte) 1;
    public final static byte WITH_SALT = (byte) 2;
    public final static byte WITH_SALT_AND_NEXT = (byte) 3;

    private final List<EncodedBodyChunk> chunks;
    private final EncodedHeadNode next;
    private final byte[] salt;

    public EncodedHeadNode(List<EncodedBodyChunk> chunks, byte[] salt, EncodedHeadNode next) {
        this.chunks = chunks;
        this.next = next;
        this.salt = salt;
    }

    private byte nodeFlag() {
        if (salt != null && next != null) {
            return WITH_SALT_AND_NEXT;
        }
        if (salt != null) {
            return WITH_SALT;
        }
        if (next != null) {
            return WITH_NEXT;
        }
        return 0;
    }

    public byte[] data() {
        int size = 1 + chunks.size() * DIGEST_SIZE;
        if (salt != null) {
            size += SALT_SIZE;
        }
        if (next != null) {
            size += DIGEST_SIZE;
        }
        byte[] data = new byte[size];
        int pos = 0;
        data[pos++] = nodeFlag();
        for (int i = 0; i < chunks.size(); i++) {
            EncodedBodyChunk chunk = chunks.get(i);
            byte[] digest = chunk.digest();
            System.arraycopy(digest, 0, data, pos, DIGEST_SIZE);
            pos += DIGEST_SIZE;
        }
        if (salt != null) {
            System.arraycopy(salt, 0, data, pos, SALT_SIZE);
            pos += SALT_SIZE;
        }
        if (next != null) {
            byte[] digest = next.digest();
            System.arraycopy(digest, 0, data, pos, DIGEST_SIZE);
        }
        return data;
    }

    public byte[] digest() {
        return Digest.sha1Bytes(data());
    }
}
