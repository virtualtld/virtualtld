package com.protocol.cdc;

import java.util.List;

import static com.protocol.cdc.CdcFileBodyChunk.DIGEST_SIZE;

class EncodedHeadNode {
    public final static byte LAST_NODE = (byte) 0;
    public final static byte FIRST_NODE = (byte) 1;
    public final static byte MIDDLE_NODE = (byte) 2;

    private final List<CdcFileBodyChunk> chunks;
    private final EncodedHeadNode next;

    public EncodedHeadNode(List<CdcFileBodyChunk> chunks, EncodedHeadNode next) {
        this.chunks = chunks;
        this.next = next;
    }

    public byte[] data() {
        int size = 1 + chunks.size() * DIGEST_SIZE;
        if (next != null) {
            size += DIGEST_SIZE;
        }
        byte[] data = new byte[size];
        data[0] = next == null ? LAST_NODE : FIRST_NODE;
        for (int i = 0; i < chunks.size(); i++) {
            CdcFileBodyChunk chunk = chunks.get(i);
            byte[] digest = chunk.digest();
            System.arraycopy(digest, 0, data, 1 + i * DIGEST_SIZE, DIGEST_SIZE);
        }
        if (next != null) {
            byte[] digest = next.digest();
            System.arraycopy(digest, 0, data, 1 + chunks.size() * DIGEST_SIZE, DIGEST_SIZE);
        }
        return data;
    }

    public byte[] digest() {
        return Digest.sha1Bytes(data());
    }
}
