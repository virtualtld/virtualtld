package com.protocol.cdc;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.copyOfRange;

public class EncodedFileBody {

    private final byte[] content;
    private final int chunkSizeLimit;
    private final Password password;
    private List<EncodedBodyChunk> cache;

    public EncodedFileBody(byte[] content, int chunkSizeLimit, Password password) {
        this.content = content;
        this.chunkSizeLimit = chunkSizeLimit;
        this.password = password;
    }

    public List<EncodedBodyChunk> body() {
        if (cache == null) {
            cache = calculateBody();
        }
        return cache;
    }

    private List<EncodedBodyChunk> calculateBody() {
        ArrayList<EncodedBodyChunk> chunks = new ArrayList<>();
        int pos = 0;
        while (pos < content.length) {
            int chunkSize = content.length - pos;
            if (chunkSize > chunkSizeLimit) {
                chunkSize = chunkSizeLimit;
            }
            byte[] data = copyOfRange(content, pos, pos + chunkSize);
            chunks.add(new EncodedBodyChunk(password, data));
            pos += chunkSize;
        }
        return chunks;
    }
}
