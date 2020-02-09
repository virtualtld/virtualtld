package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

import static com.protocol.cdc.Password.SALT_SIZE;
import static java.util.Arrays.copyOfRange;

public class EncodedFile {

    private final Password password;
    private final int chunkSizeLimit;
    private final EncodedFileBody body;
    private final EncodedFileHead head;
    private PathBlock pathBlock;

    public EncodedFile(VirtualtldSite site, String path, byte[] content) {
        byte[] salt = copyOfRange(Digest.sha1Bytes(content), 0, SALT_SIZE);
        this.password = new Password(site.publicDomain.toString(), salt);
        chunkSizeLimit = new ChunkSizeLimit(site.privateDomain).limit();
        body = new EncodedFileBody(content, chunkSizeLimit, password);
        head = new EncodedFileHead(password, chunkSizeLimit, body.body());
        pathBlock = new PathBlock(site, path, head);
    }

    public List<EncodedBodyChunk> body() {
        return body.body();
    }

    public List<EncodedHeadNode> head() {
        return head.head();
    }

    public List<Block> blocks() {
        ArrayList<Block> blocks = new ArrayList<>(body());
        blocks.addAll(head());
        blocks.add(pathBlock);
        return blocks;
    }

    private static class PathBlock implements Block {

        private final VirtualtldSite site;
        private final String path;
        private final EncodedFileHead head;

        private PathBlock(VirtualtldSite site, String path, EncodedFileHead head) {
            this.site = site;
            this.path = path;
            this.head = head;
        }

        @Override
        public String digest() {
            return new EncodedPath(site.publicDomain, path).digest();
        }

        @Override
        public byte[] data() {
            return head.head().get(0).data();
        }
    }
}
