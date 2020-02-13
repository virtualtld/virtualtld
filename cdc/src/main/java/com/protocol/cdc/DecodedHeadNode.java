package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

import static com.protocol.cdc.Digest.hex;
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
        if (!hasSalt()) {
            throw new RuntimeException("no salt");
        }
        int baseOffset = 1;
        if (hasNext()) {
            baseOffset += DIGEST_SIZE;
        }
        return copyOfRange(encodedData, baseOffset, baseOffset + SALT_SIZE);
    }

    public List<String> chunkDigests() {
        int baseOffset = 1;
        if (hasSalt()) {
            baseOffset += SALT_SIZE;
        }
        if (hasNext()) {
            baseOffset += DIGEST_SIZE;
        }
        int chunksCount = (encodedData.length - baseOffset) / DIGEST_SIZE;
        ArrayList<String> chunkDigests = new ArrayList<>();
        for (int i = 0; i < chunksCount; i++) {
            String chunkDigest = hex(encodedData, baseOffset + i * DIGEST_SIZE, DIGEST_SIZE);
            chunkDigests.add(chunkDigest);
        }
        return chunkDigests;
    }

    public String nextDigest() {
        if (!hasNext()) {
            throw new RuntimeException("no next");
        }
        return Digest.hex(copyOfRange(encodedData, 1, 1 + DIGEST_SIZE));
    }

    public boolean hasSalt() {
        return (flag() & FLAG_SALT) != 0;
    }

    public boolean hasNext() {
        return (flag() & FLAG_NEXT) != 0;
    }
}
