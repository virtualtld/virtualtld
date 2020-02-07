package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

import static com.protocol.cdc.Password.SALT_SIZE;
import static java.util.Arrays.copyOfRange;

public class EncodedFile {

    private final Password password;
    private final int chunkSizeLimit;
    private final byte[] content;

    public EncodedFile(String privateDomain, byte[] content) {
        byte[] salt = copyOfRange(Digest.sha1Bytes(content), 0, SALT_SIZE);
        this.password = new Password(privateDomain, salt);
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
        EncodedHeadNode node = new EncodedHeadNode(body(), null, null);
        nodes.add(node);
        return nodes;
    }
}
