package com.protocol.cdc;

import java.util.List;

import static com.protocol.cdc.EncodedBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.Password.SALT_SIZE;

class EncodedHeadNode {
    public final static byte LAST_NODE = (byte) 0;
    public final static byte FIRST_NODE = (byte) 1;

    private final List<EncodedBodyChunk> chunks;
    private final EncodedHeadNode next;
    private final byte[] salt;

    public EncodedHeadNode(List<EncodedBodyChunk> chunks, EncodedHeadNode next) {
        this(chunks, next, null);
    }

    public EncodedHeadNode(List<EncodedBodyChunk> chunks, EncodedHeadNode next, byte[] salt) {
        this.chunks = chunks;
        this.next = next;
        this.salt = salt;
    }

    private byte nodeFlag() {
        return next == null ? LAST_NODE : FIRST_NODE;
    }

    public byte[] data() {
        int size = 1 + chunks.size() * DIGEST_SIZE;
        if (next != null) {
            size += DIGEST_SIZE;
        }
        if (salt != null) {
            size += 8;
        }
        byte[] data = new byte[size];
        int pos = 0;
        if (salt != null) {
            System.arraycopy(salt, 0, data, 0, SALT_SIZE);
            pos += SALT_SIZE;
        }
        data[pos++] = nodeFlag();
        for (int i = 0; i < chunks.size(); i++) {
            EncodedBodyChunk chunk = chunks.get(i);
            byte[] digest = chunk.digest();
            System.arraycopy(digest, 0, data, pos, DIGEST_SIZE);
            pos += DIGEST_SIZE;
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
