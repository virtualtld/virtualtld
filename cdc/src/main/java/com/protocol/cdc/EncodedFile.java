package com.protocol.cdc;

import org.xbill.DNS.Name;

import java.util.ArrayList;
import java.util.List;

import static com.protocol.cdc.Password.SALT_SIZE;
import static java.util.Arrays.copyOfRange;

public class EncodedFile {

    public final Password password;
    private final EncodedFileBody body;
    private final EncodedFileHead head;
    private PathBlock pathBlock;

    public EncodedFile(Name publicDomain, String path, byte[] content, int chunkSizeLimit) {
        byte[] salt = copyOfRange(Digest.sha1Bytes(content), 0, SALT_SIZE);
        this.password = new Password(publicDomain.toString(), salt);
        body = new EncodedFileBody(content, chunkSizeLimit, password);
        head = new EncodedFileHead(password, chunkSizeLimit, body.body());
        pathBlock = new PathBlock(publicDomain, path, head);
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

        private final Name publicDomain;
        private final String path;
        private final EncodedFileHead head;

        private PathBlock(Name publicDomain, String path, EncodedFileHead head) {
            this.publicDomain = publicDomain;
            this.path = path;
            this.head = head;
        }

        @Override
        public String digest() {
            return new EncodedPath(publicDomain, path).digest();
        }

        @Override
        public byte[] data() {
            return head.head().get(0).data();
        }

        @Override
        public int ttl() {
            return 60;
        }
    }
}
