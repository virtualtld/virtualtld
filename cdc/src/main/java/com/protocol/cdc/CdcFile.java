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

    public List<CdcFileBodyChunk> chunks() {
        ArrayList<CdcFileBodyChunk> chunks = new ArrayList<>();
        int pos = 0;
        int chunkSize = content.length - pos;
        byte[] data = new byte[chunkSize];
        System.arraycopy(content, pos, data, 0, chunkSize);
        chunks.add(new CdcFileBodyChunk(data));
        return chunks;
    }
}
