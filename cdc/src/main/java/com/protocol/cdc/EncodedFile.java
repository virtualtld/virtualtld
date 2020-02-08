package com.protocol.cdc;

import org.xbill.DNS.Name;

import java.util.ArrayList;
import java.util.List;

import static com.protocol.cdc.EncodedBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.Password.SALT_SIZE;
import static java.util.Arrays.copyOfRange;

public class EncodedFile {

    private final Name privateDomain;
    private final Password password;
    private final int chunkSizeLimit;
    private final byte[] content;

    public EncodedFile(CdcSite site, byte[] content) {
        byte[] salt = copyOfRange(Digest.sha1Bytes(content), 0, SALT_SIZE);
        this.password = new Password(site.publicDomain.toString(), salt);
        this.privateDomain = site.privateDomain;
        chunkSizeLimit = new ChunkSizeLimit(site.privateDomain).limit();
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
            byte[] data = copyOfRange(content, pos, pos + chunkSize);
            chunks.add(new EncodedBodyChunk(privateDomain, password, data));
            pos += chunkSize;
        }
        return chunks;
    }

    public List<EncodedHeadNode> head() {
        int chunksCountLimit = (chunkSizeLimit - 1 - SALT_SIZE) / DIGEST_SIZE;
        int firstNodeChunksCountLimit = chunksCountLimit;
        List<EncodedBodyChunk> chunks = body();
        ArrayList<EncodedHeadNode> nodes = new ArrayList<>();
        if (chunks.size() <= firstNodeChunksCountLimit) {
            nodes.add(new EncodedHeadNode(privateDomain, body(), password.salt));
            return nodes;
        }
        // need room to save pointer to next node
        firstNodeChunksCountLimit -= 1;
        nodes.add(new EncodedHeadNode(privateDomain, chunks.subList(0, firstNodeChunksCountLimit), password.salt));
        chunks = chunks.subList(firstNodeChunksCountLimit, chunks.size());
        while (!chunks.isEmpty()) {
            if (chunksCountLimit > chunks.size()) {
                chunksCountLimit = chunks.size();
            }
            nodes.add(new EncodedHeadNode(privateDomain, chunks.subList(0, chunksCountLimit), null));
            chunks = chunks.subList(chunksCountLimit, chunks.size());
        }
        for (int i = 0; i < nodes.size() - 1; i++) {
            nodes.get(i).setNext(nodes.get(i + 1));
        }
        return nodes;
    }
}
