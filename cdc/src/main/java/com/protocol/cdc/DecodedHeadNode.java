package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

import static com.protocol.cdc.Digest.base64;
import static com.protocol.cdc.EncodedBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.EncodedHeadNode.FLAG_NEXT;
import static com.protocol.cdc.EncodedHeadNode.FLAG_SALT;
import static com.protocol.cdc.Password.SALT_SIZE;
import static java.util.Arrays.copyOfRange;

public class DecodedHeadNode {
    private final byte[] encodedData;

    public DecodedHeadNode(byte[] encodedData) {
        this.encodedData = encodedData;
    }

    public byte flag() {
        return encodedData[0];
    }

    public byte[] salt() {
        if ((flag() & FLAG_SALT) == 0) {
            throw new RuntimeException("no salt");
        }
        int baseOffset = 1;
        if ((flag() & FLAG_NEXT) != 0) {
            baseOffset += DIGEST_SIZE;
        }
        return copyOfRange(encodedData, baseOffset, baseOffset + SALT_SIZE);
    }

    public List<String> chunkDigests() {
        int baseOffset = 1;
        if ((flag() & FLAG_SALT) != 0) {
            baseOffset += SALT_SIZE;
        }
        if ((flag() & FLAG_NEXT) != 0) {
            baseOffset += DIGEST_SIZE;
        }
        int chunksCount = (encodedData.length - baseOffset) / DIGEST_SIZE;
        ArrayList<String> chunkDigests = new ArrayList<>();
        for (int i = 0; i < chunksCount; i++) {
            String chunkDigest = base64(encodedData, baseOffset + i * DIGEST_SIZE, DIGEST_SIZE);
            chunkDigests.add(chunkDigest);
        }
        return chunkDigests;
    }

    public String nextDigest() {
        if ((flag() & FLAG_NEXT) == 0) {
            throw new RuntimeException("no next");
        }
        return base64(copyOfRange(encodedData, 1, 1 + DIGEST_SIZE));
    }
}
