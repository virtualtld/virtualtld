package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

public class CdcFile {

    private final Password password;
    private final int chunkSizeLimit;
    private final byte[] content;

    public CdcFile(String privateDomain, byte[] content) {
        this.password = new Password(privateDomain);
        chunkSizeLimit = new ChunkSizeLimit(privateDomain).limit();
        this.content = content;
    }

    public List<EncodedBodyChunk> body() {
        ArrayList<EncodedBodyChunk> chunks = new ArrayList<>();
        int pos = 0;
        while (pos < content.length) {
            int chunkSize = content.length - pos;
            if (chunkSize > chunkSizeLimit) {
                chunkSize = chunkSizeLimit;
            }
            byte[] data = new byte[chunkSize];
            System.arraycopy(content, pos, data, 0, chunkSize);
            chunks.add(new EncodedBodyChunk(password, data));
            pos += chunkSize;
        }
        return chunks;
    }

    public List<EncodedHeadNode> head() {
        ArrayList<EncodedHeadNode> nodes = new ArrayList<>();
        EncodedHeadNode node = new EncodedHeadNode(body(), null);
        nodes.add(node);
        return nodes;
    }
}
