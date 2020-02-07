package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

import static com.protocol.cdc.EncodedBodyChunk.DIGEST_SIZE;
import static com.protocol.cdc.EncodedHeadNode.WITH_SALT;
import static com.protocol.cdc.EncodedHeadNode.WITH_SALT_AND_NEXT;
import static com.protocol.cdc.Password.SALT_SIZE;
import static java.util.Arrays.copyOfRange;

public class DecodedHeadNode {
    private final byte[] encodedData;

    public DecodedHeadNode(byte[] encodedData) {
        this.encodedData = encodedData;
    }

    public byte[] salt() {
        if (encodedData[0] == WITH_SALT) {
            return copyOfRange(encodedData, encodedData.length - SALT_SIZE, encodedData.length);
        }
        if (encodedData[0] == WITH_SALT_AND_NEXT) {
            throw new RuntimeException("not implemented");
        }
        throw new RuntimeException("no salt");
    }


    public List<String> chunkDigests() {
        int chunksCount = (encodedData.length - SALT_SIZE - 1) / DIGEST_SIZE;
        ArrayList<String> chunkDigests = new ArrayList<>();
        for (int i = 0; i < chunksCount; i++) {
            String chunkDigest = Digest.base64(encodedData, i * DIGEST_SIZE, DIGEST_SIZE);
            chunkDigests.add(chunkDigest);
        }
        return chunkDigests;
    }
}
