package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

import static com.protocol.cdc.EncodedBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.Password.SALT_SIZE;

public class EncodedFileHead {

    private final Password password;
    private final int chunkSizeLimit;
    private final List<EncodedBodyChunk> body;
    private List<EncodedHeadNode> cache;

    public EncodedFileHead(Password password, int chunkSizeLimit, List<EncodedBodyChunk> body) {
        this.password = password;
        this.chunkSizeLimit = chunkSizeLimit;
        this.body = body;
    }

    public List<EncodedHeadNode> head() {
        if (cache == null) {
            cache = calculateHead();
        }
        return cache;
    }

    private List<EncodedHeadNode> calculateHead() {
        int chunksCountLimit = (chunkSizeLimit - 1 - SALT_SIZE) / DIGEST_SIZE;
        int firstNodeChunksCountLimit = chunksCountLimit;
        List<EncodedBodyChunk> chunks = body;
        ArrayList<EncodedHeadNode> nodes = new ArrayList<>();
        if (chunks.size() <= firstNodeChunksCountLimit) {
            nodes.add(new EncodedHeadNode(chunks, password.salt));
            return nodes;
        }
        // need room to save pointer to next node
        firstNodeChunksCountLimit -= 1;
        nodes.add(new EncodedHeadNode(chunks.subList(0, firstNodeChunksCountLimit), password.salt));
        chunks = chunks.subList(firstNodeChunksCountLimit, chunks.size());
        while (!chunks.isEmpty()) {
            if (chunksCountLimit > chunks.size()) {
                chunksCountLimit = chunks.size();
            }
            nodes.add(new EncodedHeadNode(chunks.subList(0, chunksCountLimit), null));
            chunks = chunks.subList(chunksCountLimit, chunks.size());
        }
        for (int i = 0; i < nodes.size() - 1; i++) {
            nodes.get(i).setNext(nodes.get(i + 1));
        }
        return nodes;
    }
}
