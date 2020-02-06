package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

public class CdcFile {

    private final String privateDomain;
    private final int chunkSizeLimit;
    private final byte[] content;

    public CdcFile(String privateDomain, byte[] content) {
        this.privateDomain = privateDomain;
        chunkSizeLimit = new ChunkSizeLimit(privateDomain).limit();
        this.content = content;
    }

    public List<CdcFileBodyChunk> body() {
        ArrayList<CdcFileBodyChunk> chunks = new ArrayList<>();
        int pos = 0;
        while (pos < content.length) {
            int chunkSize = content.length - pos;
            if (chunkSize > chunkSizeLimit) {
                chunkSize = chunkSizeLimit;
            }
            byte[] data = new byte[chunkSize];
            System.arraycopy(content, pos, data, 0, chunkSize);
            chunks.add(new CdcFileBodyChunk(data));
            pos += chunkSize;
        }
        return chunks;
    }

    public List<CdcFileHeadNode> head() {
        ArrayList<CdcFileHeadNode> nodes = new ArrayList<>();
        CdcFileHeadNode node = new CdcFileHeadNode(body(), null);
        nodes.add(node);
        return nodes;
    }
}
